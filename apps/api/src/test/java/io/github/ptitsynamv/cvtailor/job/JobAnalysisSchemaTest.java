package io.github.ptitsynamv.cvtailor.job;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.anthropic.models.messages.MessageCreateParams;

/**
 * Builds the structured-output request the real service sends. The SDK derives the JSON schema from the record and
 * validates it locally, so a record the API can't accept fails here, without an API key.
 */
class JobAnalysisSchemaTest {

	@Test
	void derivesAValidSchemaWithFieldDescriptionsAndSeniorityValues() {
		var params = MessageCreateParams.builder()
				.model("claude-opus-5-5")
				.maxTokens(1L)
				.outputConfig(JobAnalysis.class)
				.addUserMessage("job text")
				.build();

		String schema = params.rawParams().outputConfig().orElseThrow().toString();
		assertThat(schema).contains("mustHave", "niceToHave", "keywords", "NOT_STATED", "Required qualifications");
	}

}
