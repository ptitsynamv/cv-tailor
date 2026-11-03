package io.github.ptitsynamv.cvtailor.documents;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.ptitsynamv.cvtailor.documents.DocumentsDraft.CvDraft;
import io.github.ptitsynamv.cvtailor.documents.DocumentsDraft.RoleDraft;
import io.github.ptitsynamv.cvtailor.profile.Profile;
import io.github.ptitsynamv.cvtailor.profile.ProfileItem;
import io.github.ptitsynamv.cvtailor.profile.ProfileSection;

/** Builds the {@link TailoredCv}: facts from the profile, text from a verified draft. */
final class CvAssembler {

	private CvAssembler() {
	}

	static TailoredCv assemble(CvDraft draft, Profile profile) {
		List<TailoredCv.Role> roles = new ArrayList<>();
		for (RoleDraft role : draft.experience()) {
			profile.item(role.roleId()).ifPresent(item -> roles.add(role(item, role.bullets())));
		}
		// A CV must not hide part of the work history: roles Claude skipped are listed without bullets.
		Set<String> included = draft.experience().stream().map(RoleDraft::roleId).collect(Collectors.toSet());
		profile.items(ProfileSection.EXPERIENCE).stream()
				.filter(item -> !item.fields().isEmpty() && !included.contains(item.id()))
				.forEach(item -> roles.add(role(item, List.of())));

		List<TailoredCv.Project> projects = draft.projects().stream()
				.flatMap(project -> profile.item(project.projectId()).stream()
						.map(item -> new TailoredCv.Project(item.id(), item.field("Project Name"),
								item.field("Primary Stack"), project.bullets())))
				.toList();

		List<String> certificates = profile.items(ProfileSection.CERTIFICATE).stream()
				.map(item -> join(", ", item.field("Certification"), item.field("Issuer")))
				.toList();
		List<String> education = profile.items(ProfileSection.EDUCATION).stream()
				.map(item -> join(", ", item.field("Official Degree Title"), item.field("University / Institution"),
						item.field("Years Attended")))
				.toList();

		return new TailoredCv(profile.contact(), draft.summary(), draft.skills(), roles, projects, certificates,
				education);
	}

	private static TailoredCv.Role role(ProfileItem item, List<CvBullet> bullets) {
		return new TailoredCv.Role(item.id(), item.field("Role / Title"), item.field("Company"), item.field("Period"),
				item.field("Location"), bullets);
	}

	private static String join(String separator, String... parts) {
		return Stream.of(parts).filter(part -> !part.isBlank()).collect(Collectors.joining(separator));
	}

}
