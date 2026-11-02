package io.github.ptitsynamv.cvtailor.gap;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record GapReport(@JsonPropertyDescription("One item per requirement, in the given order") List<GapItem> items) {

	public GapReport {
		items = List.copyOf(items);
	}

}
