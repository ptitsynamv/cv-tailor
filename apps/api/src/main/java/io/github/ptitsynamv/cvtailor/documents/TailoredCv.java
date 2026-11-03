package io.github.ptitsynamv.cvtailor.documents;

import java.util.List;

import io.github.ptitsynamv.cvtailor.profile.Contact;

/**
 * The finished CV. Contact details, role and project facts, certificates and education come from the profile;
 * the summary, skill groups and bullets come from Claude, after {@link CvGuardrail}.
 */
public record TailoredCv(Contact contact, List<CvBullet> summary, List<CvSkillGroup> skills, List<Role> experience,
		List<Project> projects, List<String> certificates, List<String> education) {

	public TailoredCv {
		summary = List.copyOf(summary);
		skills = List.copyOf(skills);
		experience = List.copyOf(experience);
		projects = List.copyOf(projects);
		certificates = List.copyOf(certificates);
		education = List.copyOf(education);
	}

	public record Role(String id, String title, String company, String period, String location, List<CvBullet> bullets) {

		public Role {
			bullets = List.copyOf(bullets);
		}

	}

	public record Project(String id, String name, String stack, List<CvBullet> bullets) {

		public Project {
			bullets = List.copyOf(bullets);
		}

	}

}
