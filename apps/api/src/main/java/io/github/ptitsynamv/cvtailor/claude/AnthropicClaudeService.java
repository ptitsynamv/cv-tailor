package io.github.ptitsynamv.cvtailor.claude;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anthropic.client.AnthropicClient;
import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.CacheControlEphemeral;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.StructuredMessage;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.anthropic.models.messages.StructuredTextBlock;
import com.anthropic.models.messages.TextBlockParam;
import com.anthropic.models.messages.Usage;

/** Calls the Claude API with structured outputs, caching the profile block across requests. */
public class AnthropicClaudeService implements ClaudeService {

	/** If a safety classifier declines a request, the API retries it on a fallback model instead of just stopping. */
	static final String FALLBACK_BETA = "server-side-fallback-2026-07-01";

	private static final Logger log = LoggerFactory.getLogger(AnthropicClaudeService.class);

	private final AnthropicClient client;
	private final ClaudeProperties properties;

	public AnthropicClaudeService(AnthropicClient client, ClaudeProperties properties) {
		this.client = client;
		this.properties = properties;
	}

	@Override
	public <T> T generate(ClaudeRequest request, Class<T> outputType) {
		StructuredMessage<T> response = client.messages().create(buildParams(request, outputType));
		logUsage(request.step(), response.usage());

		StopReason stopReason = response.stopReason().orElse(null);
		if (!StopReason.END_TURN.equals(stopReason)) {
			String details = response.stopDetails()
					.map(d -> " (" + d.category().map(Object::toString).orElse("?") + ": " + d.explanation().orElse("") + ")")
					.orElse("");
			throw new ClaudeException("Claude stopped with %s%s at step '%s'".formatted(stopReason, details, request.step()));
		}
		return response.content().stream()
				.flatMap(block -> block.text().stream())
				.map(StructuredTextBlock::text)
				.findFirst()
				.orElseThrow(() -> new ClaudeException("Claude returned no structured output at step '%s'".formatted(request.step())));
	}

	<T> StructuredMessageCreateParams<T> buildParams(ClaudeRequest request, Class<T> outputType) {
		// The profile goes first: the cache is a prefix match, so every step that sends the same profile reuses it.
		List<TextBlockParam> system = new ArrayList<>();
		if (request.profile() != null) {
			system.add(TextBlockParam.builder()
					.text(request.profile())
					.cacheControl(CacheControlEphemeral.builder().build())
					.build());
		}
		system.add(TextBlockParam.builder().text(request.instructions()).build());

		return MessageCreateParams.builder()
				.model(properties.model())
				.maxTokens(properties.maxTokens())
				.outputConfig(outputType)
				.systemOfTextBlockParams(system)
				.addUserMessage(request.userMessage())
				.putAdditionalHeader("anthropic-beta", FALLBACK_BETA)
				.putAdditionalBodyProperty("fallbacks", JsonValue.from("default"))
				.build();
	}

	private static void logUsage(String step, Usage usage) {
		log.info("Claude step '{}': input={} cacheRead={} cacheWrite={} output={} tokens", step, usage.inputTokens(),
				usage.cacheReadInputTokens().orElse(0L), usage.cacheCreationInputTokens().orElse(0L),
				usage.outputTokens());
	}

}
