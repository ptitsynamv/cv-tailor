package io.github.ptitsynamv.cvtailor.documents;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record CvSkillGroup(
		@JsonPropertyDescription("ATS-friendly group name, e.g. Frontend, Testing") String name,
		@JsonPropertyDescription("Skill names, using the job's terms where the profile supports them") List<String> skills,
		@JsonPropertyDescription("Ids of the profile lines that show these skills, copied exactly") List<String> evidenceIds) {

	public CvSkillGroup {
		skills = List.copyOf(skills);
		evidenceIds = List.copyOf(evidenceIds);
	}

}
