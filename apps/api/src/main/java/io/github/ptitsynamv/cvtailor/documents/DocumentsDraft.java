package io.github.ptitsynamv.cvtailor.documents;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * What Claude writes (via structured outputs): only text, with every claim citing profile ids. Roles and projects are
 * referenced by id; their names, titles and dates are copied from the profile in code ({@link CvAssembler}).
 */
public record DocumentsDraft(CvDraft cv, CoverLetter coverLetter) {

	public record CvDraft(
			@JsonPropertyDescription("2-3 sentences tailored to the job") List<CvBullet> summary,
			@JsonPropertyDescription("3-5 skill groups") List<CvSkillGroup> skills,
			@JsonPropertyDescription("Every role in the profile, most recent first") List<RoleDraft> experience,
			@JsonPropertyDescription("0-2 projects relevant to the job") List<ProjectDraft> projects) {

		public CvDraft {
			summary = List.copyOf(summary);
			skills = List.copyOf(skills);
			experience = List.copyOf(experience);
			projects = List.copyOf(projects);
		}

	}

	public record RoleDraft(
			@JsonPropertyDescription("The role's id: the exp- id without a number, e.g. exp-acme") String roleId,
			@JsonPropertyDescription("Achievement bullets, each starting with a strong verb") List<CvBullet> bullets) {

		public RoleDraft {
			bullets = List.copyOf(bullets);
		}

	}

	public record ProjectDraft(
			@JsonPropertyDescription("The project's proj- id") String projectId,
			@JsonPropertyDescription("1-2 bullets") List<CvBullet> bullets) {

		public ProjectDraft {
			bullets = List.copyOf(bullets);
		}

	}

}
