package io.github.ptitsynamv.cvtailor.gap;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record GapItem(
		@JsonPropertyDescription("The requirement, copied unchanged from the list") String requirement,
		@JsonPropertyDescription("STRONG: shown directly at the level asked. PARTIAL: related or weaker evidence. GAP: no evidence") GapStatus status,
		@JsonPropertyDescription("Ids of the profile lines that support the match, copied exactly; empty for a GAP") List<String> evidenceIds,
		@JsonPropertyDescription("One short sentence on why; for PARTIAL and GAP, say what is missing") String note) {

	public GapItem {
		evidenceIds = List.copyOf(evidenceIds);
	}

}
