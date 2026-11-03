package io.github.ptitsynamv.cvtailor.documents;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.ptitsynamv.cvtailor.documents.DocumentsDraft.CvDraft;
import io.github.ptitsynamv.cvtailor.documents.DocumentsDraft.ProjectDraft;
import io.github.ptitsynamv.cvtailor.documents.DocumentsDraft.RoleDraft;
import io.github.ptitsynamv.cvtailor.profile.Profile;
import io.github.ptitsynamv.cvtailor.profile.ProfileItem;
import io.github.ptitsynamv.cvtailor.profile.ProfileSection;

/**
 * The no-invention guardrail for the CV: every claim must cite ids that exist in the profile, or it's removed.
 *
 * <ul>
 * <li>A summary sentence, skill group or bullet citing no ids, or any unknown id, is dropped.</li>
 * <li>A role whose id isn't a role in the profile is dropped, and so is a project whose id isn't a project.</li>
 * <li>A project left without bullets is dropped; a role is kept, since the work history must stay complete.</li>
 * </ul>
 */
final class CvGuardrail {

	private static final Logger log = LoggerFactory.getLogger(CvGuardrail.class);

	private CvGuardrail() {
	}

	static CvDraft verify(CvDraft cv, Profile profile) {
		Set<String> ids = profile.ids();
		Set<String> roleIds = idsOf(profile, ProfileSection.EXPERIENCE, true);
		Set<String> projectIds = idsOf(profile, ProfileSection.PROJECT, false);

		List<CvSkillGroup> skills = cv.skills().stream().filter(group -> {
			boolean cited = isCited(group.evidenceIds(), ids);
			if (!cited) {
				log.warn("Guardrail: dropped skill group '{}' citing {}", group.name(), group.evidenceIds());
			}
			return cited;
		}).toList();

		List<RoleDraft> roles = cv.experience().stream().filter(role -> {
			boolean known = roleIds.contains(role.roleId());
			if (!known) {
				log.warn("Guardrail: dropped role '{}', not a role in the profile", role.roleId());
			}
			return known;
		}).map(role -> new RoleDraft(role.roleId(), verified(role.bullets(), ids))).toList();

		List<ProjectDraft> projects = cv.projects().stream().filter(project -> {
			boolean known = projectIds.contains(project.projectId());
			if (!known) {
				log.warn("Guardrail: dropped project '{}', not a project in the profile", project.projectId());
			}
			return known;
		}).map(project -> new ProjectDraft(project.projectId(), verified(project.bullets(), ids)))
				.filter(project -> !project.bullets().isEmpty())
				.toList();

		return new CvDraft(verified(cv.summary(), ids), skills, roles, projects);
	}

	private static List<CvBullet> verified(List<CvBullet> bullets, Set<String> ids) {
		return bullets.stream().filter(bullet -> {
			boolean cited = isCited(bullet.evidenceIds(), ids);
			if (!cited) {
				log.warn("Guardrail: dropped '{}' citing {}", bullet.text(), bullet.evidenceIds());
			}
			return cited;
		}).toList();
	}

	private static boolean isCited(List<String> evidenceIds, Set<String> ids) {
		return !evidenceIds.isEmpty() && ids.containsAll(evidenceIds);
	}

	/** Roles are the experience items that carry facts (the {@code exp-<folder>} ids), not their bullets. */
	private static Set<String> idsOf(Profile profile, ProfileSection section, boolean withFieldsOnly) {
		return profile.items(section).stream()
				.filter(item -> !withFieldsOnly || !item.fields().isEmpty())
				.map(ProfileItem::id)
				.collect(Collectors.toUnmodifiableSet());
	}

}
