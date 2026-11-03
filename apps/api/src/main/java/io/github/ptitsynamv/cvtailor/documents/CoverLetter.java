package io.github.ptitsynamv.cvtailor.documents;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record CoverLetter(
		@JsonPropertyDescription("Salutation line, e.g. \"Dear Hiring Team,\"") String greeting,
		@JsonPropertyDescription("Body paragraphs, without a sign-off or the candidate's name") List<String> paragraphs) {

	public CoverLetter {
		paragraphs = List.copyOf(paragraphs);
	}

	public int wordCount() {
		return paragraphs.stream().mapToInt(paragraph -> paragraph.split("\\s+").length).sum();
	}

}
