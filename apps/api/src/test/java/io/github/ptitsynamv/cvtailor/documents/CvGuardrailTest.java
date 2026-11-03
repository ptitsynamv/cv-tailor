package io.github.ptitsynamv.cvtailor.documents;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.ptitsynamv.cvtailor.documents.DocumentsDraft.CvDraft;
import io.github.ptitsynamv.cvtailor.documents.DocumentsDraft.ProjectDraft;
import io.github.ptitsynamv.cvtailor.documents.DocumentsDraft.RoleDraft;
import io.github.ptitsynamv.cvtailor.profile.Profile;
import io.github.ptitsynamv.cvtailor.profile.ProfileLoader;
import io.github.ptitsynamv.cvtailor.profile.ProfileProperties;

class CvGuardrailTest {

	private static final Profile PROFILE =
			new ProfileLoader(new ProfileProperties("../../demo-profile")).load();

	private static final CvBullet VALID = new CvBullet("Cut the bundle by 38%.", List.of("exp-lumenledger-10"));
	private static final CvBullet INVENTED = new CvBullet("Shipped a React app.", List.of("exp-lumenledger-99"));
	private static final CvBullet UNCITED = new CvBullet("Expert in everything.", List.of());

	@Test
	void keepsOnlyClaimsWhoseEvidenceExists() {
		var draft = new CvDraft(List.of(VALID, INVENTED, UNCITED), List.of(),
				List.of(new RoleDraft("exp-lumenledger", List.of(INVENTED, VALID, UNCITED))), List.of());

		CvDraft verified = CvGuardrail.verify(draft, PROFILE);

		assertThat(verified.summary()).containsExactly(VALID);
		assertThat(verified.experience().getFirst().bullets()).containsExactly(VALID);
	}

	@Test
	void dropsASkillGroupWithAnyUnknownId() {
		var valid = new CvSkillGroup("Frontend", List.of("Angular"), List.of("skill-angular"));
		var invented = new CvSkillGroup("Backend", List.of("Java"), List.of("skill-angular", "skill-java"));

		CvDraft verified = CvGuardrail.verify(new CvDraft(List.of(), List.of(valid, invented), List.of(), List.of()),
				PROFILE);

		assertThat(verified.skills()).containsExactly(valid);
	}

	@Test
	void dropsRolesAndProjectsThatAreNotInTheProfile() {
		var draft = new CvDraft(List.of(), List.of(),
				List.of(new RoleDraft("exp-lumenledger", List.of(VALID)), new RoleDraft("exp-google", List.of(VALID)),
						new RoleDraft("exp-lumenledger-3", List.of(VALID))),
				List.of(new ProjectDraft("proj-budget-buddy", List.of(VALID)),
						new ProjectDraft("proj-ai-code-reviewer", List.of(VALID))));

		CvDraft verified = CvGuardrail.verify(draft, PROFILE);

		assertThat(verified.experience()).extracting(RoleDraft::roleId).containsExactly("exp-lumenledger");
		assertThat(verified.projects()).extracting(ProjectDraft::projectId).containsExactly("proj-budget-buddy");
	}

	@Test
	void dropsAProjectLeftWithoutBulletsButKeepsTheRole() {
		var draft = new CvDraft(List.of(), List.of(), List.of(new RoleDraft("exp-lumenledger", List.of(INVENTED))),
				List.of(new ProjectDraft("proj-budget-buddy", List.of(INVENTED))));

		CvDraft verified = CvGuardrail.verify(draft, PROFILE);

		assertThat(verified.experience()).hasSize(1);
		assertThat(verified.experience().getFirst().bullets()).isEmpty();
		assertThat(verified.projects()).isEmpty();
	}

}
