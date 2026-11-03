package io.github.ptitsynamv.cvtailor.documents;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/** One CV claim (a bullet or a summary sentence) and the profile lines it's based on. */
public record CvBullet(
		@JsonPropertyDescription("The sentence, reworded from the profile without inflating it") String text,
		@JsonPropertyDescription("Ids of the profile lines this is based on, copied exactly") List<String> evidenceIds) {

	public CvBullet {
		evidenceIds = List.copyOf(evidenceIds);
	}

}
