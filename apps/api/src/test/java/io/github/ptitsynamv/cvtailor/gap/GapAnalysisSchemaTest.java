package io.github.ptitsynamv.cvtailor.gap;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.anthropic.models.messages.MessageCreateParams;

/** The SDK derives and validates the structured-output schema locally, so this fails without an API key if it's wrong. */
class GapAnalysisSchemaTest {

	@Test
	void derivesAValidSchemaForTheGapReport() {
		var params = MessageCreateParams.builder()
				.model("claude-opus-5-5")
				.maxTokens(1L)
				.outputConfig(GapReport.class)
				.addUserMessage("requirements")
				.build();

		String schema = params.rawParams().outputConfig().orElseThrow().toString();
		assertThat(schema).contains("evidenceIds", "STRONG", "PARTIAL", "GAP", "copied exactly");
	}

}
