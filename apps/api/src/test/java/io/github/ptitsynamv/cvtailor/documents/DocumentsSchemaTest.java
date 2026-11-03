package io.github.ptitsynamv.cvtailor.documents;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.anthropic.models.messages.MessageCreateParams;

/** The SDK derives and validates the structured-output schema locally, so this fails without an API key if it's wrong. */
class DocumentsSchemaTest {

	@Test
	void derivesAValidSchemaForTheDocumentsDraft() {
		var params = MessageCreateParams.builder()
				.model("claude-opus-5-5")
				.maxTokens(1L)
				.outputConfig(DocumentsDraft.class)
				.addUserMessage("job")
				.build();

		String schema = params.rawParams().outputConfig().orElseThrow().toString();
		assertThat(schema).contains("roleId", "projectId", "evidenceIds", "paragraphs", "copied exactly");
	}

}
