package io.github.ptitsynamv.cvtailor.job;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * What a job post asks for. Claude fills it via structured outputs: the JSON schema, including these descriptions,
 * is derived from this record.
 */
public record JobAnalysis(
		@JsonPropertyDescription("Job title exactly as written in the post") String title,
		@JsonPropertyDescription("Hiring company name, or an empty string if the post doesn't name it") String company,
		@JsonPropertyDescription("Seniority from the title or required years of experience; NOT_STATED if neither says") Seniority seniority,
		@JsonPropertyDescription("Required qualifications, one requirement per item") List<String> mustHave,
		@JsonPropertyDescription("Optional qualifications, one per item") List<String> niceToHave,
		@JsonPropertyDescription("Technologies, tools, methods and domains named in the post, as written, without duplicates") List<String> keywords) {

	public JobAnalysis {
		mustHave = List.copyOf(mustHave);
		niceToHave = List.copyOf(niceToHave);
		keywords = List.copyOf(keywords);
	}

}
