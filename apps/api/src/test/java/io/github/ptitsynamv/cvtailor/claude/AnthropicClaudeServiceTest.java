package io.github.ptitsynamv.cvtailor.claude;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.anthropic.client.AnthropicClient;
import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.StructuredContentBlock;
import com.anthropic.models.messages.StructuredMessage;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.anthropic.models.messages.StructuredTextBlock;
import com.anthropic.models.messages.TextBlockParam;
import com.anthropic.models.messages.Usage;
import com.anthropic.services.blocking.MessageService;

class AnthropicClaudeServiceTest {

	record Sample(String title) {
	}

	private final AnthropicClient client = mock(AnthropicClient.class);
	private final MessageService messages = mock(MessageService.class);
	private final AnthropicClaudeService service =
			new AnthropicClaudeService(client, new ClaudeProperties("claude-opus-5-5", 16000));

	private final ClaudeRequest request = new ClaudeRequest("job-analysis", "Extract requirements.", "[exp-a] profile",
			"job text");

	@Test
	void returnsTheStructuredOutputWhenClaudeFinishes() {
		respondWith(StopReason.END_TURN, new Sample("Senior Frontend Engineer"));

		Sample result = service.generate(request, Sample.class);

		assertThat(result).isEqualTo(new Sample("Senior Frontend Engineer"));
	}

	@Test
	void throwsInsteadOfReadingContentWhenClaudeStopsEarly() {
		respondWith(StopReason.MAX_TOKENS, new Sample("cut off"));

		assertThatThrownBy(() -> service.generate(request, Sample.class))
				.isInstanceOf(ClaudeException.class)
				.hasMessageContaining("max_tokens")
				.hasMessageContaining("job-analysis");
	}

	@Test
	void sendsTheProfileFirstAndMarksItForCaching() {
		StructuredMessageCreateParams<Sample> params = service.buildParams(request, Sample.class);

		List<TextBlockParam> system = params.rawParams().system().orElseThrow().asTextBlockParams();
		assertThat(system).extracting(TextBlockParam::text).containsExactly("[exp-a] profile", "Extract requirements.");
		assertThat(system.get(0).cacheControl()).isPresent();
		assertThat(system.get(1).cacheControl()).isEmpty();
	}

	@Test
	void skipsTheProfileBlockWhenTheStepDoesNotNeedIt() {
		var withoutProfile = new ClaudeRequest("job-analysis", "Extract requirements.", null, "job text");

		List<TextBlockParam> system = service.buildParams(withoutProfile, Sample.class)
				.rawParams().system().orElseThrow().asTextBlockParams();

		assertThat(system).extracting(TextBlockParam::text).containsExactly("Extract requirements.");
	}

	@Test
	void optsIntoServerSideRefusalFallbacks() {
		var params = service.buildParams(request, Sample.class).rawParams();

		assertThat(params._additionalBodyProperties()).containsEntry("fallbacks", JsonValue.from("default"));
	}

	@SuppressWarnings("unchecked")
	private void respondWith(StopReason stopReason, Sample output) {
		StructuredTextBlock<Sample> textBlock = mock(StructuredTextBlock.class);
		when(textBlock.text()).thenReturn(output);
		StructuredContentBlock<Sample> block = mock(StructuredContentBlock.class);
		when(block.text()).thenReturn(Optional.of(textBlock));

		StructuredMessage<Sample> response = mock(StructuredMessage.class);
		when(response.stopReason()).thenReturn(Optional.of(stopReason));
		when(response.content()).thenReturn(List.of(block));
		when(response.usage()).thenReturn(mock(Usage.class));

		when(client.messages()).thenReturn(messages);
		when(messages.create(any(StructuredMessageCreateParams.class))).thenReturn(response);
	}

}
