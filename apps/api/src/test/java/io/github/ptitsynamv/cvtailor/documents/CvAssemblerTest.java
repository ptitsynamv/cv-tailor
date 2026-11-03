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

class CvAssemblerTest {

	private static final Profile PROFILE =
			new ProfileLoader(new ProfileProperties("../../demo-profile")).load();

	private static final CvBullet BULLET = new CvBullet("Cut the bundle by 38%.", List.of("exp-lumenledger-10"));

	private final TailoredCv cv = CvAssembler.assemble(new CvDraft(List.of(BULLET), List.of(),
			List.of(new RoleDraft("exp-lumenledger", List.of(BULLET))),
			List.of(new ProjectDraft("proj-budget-buddy", List.of(BULLET)))), PROFILE);

	@Test
	void copiesRoleFactsFromTheProfileNotTheModel() {
		TailoredCv.Role role = cv.experience().getFirst();

		assertThat(role.title()).isEqualTo("Senior Frontend Engineer / Frontend Team Lead");
		assertThat(role.company()).isEqualTo("Lumen Ledger");
		assertThat(role.period()).isEqualTo("Apr 2022 – Present");
		assertThat(role.location()).isEqualTo("Remote, EU");
		assertThat(role.bullets()).containsExactly(BULLET);
	}

	@Test
	void listsRolesTheModelSkippedSoTheHistoryStaysComplete() {
		assertThat(cv.experience()).extracting(TailoredCv.Role::company)
				.containsExactly("Lumen Ledger", "Brightpath Logistics");
		assertThat(cv.experience().get(1).bullets()).isEmpty();
	}

	@Test
	void copiesContactProjectsCertificatesAndEducationFromTheProfile() {
		assertThat(cv.contact().name()).isEqualTo("Alex Doe");
		assertThat(cv.contact().details()).contains("alex.doe@example.com", "linkedin.com/in/alex-doe-demo");
		assertThat(cv.projects().getFirst().name()).isEqualTo("budget-buddy");
		assertThat(cv.projects().getFirst().stack()).isEqualTo("Angular 20, Signals, Tailwind");
		assertThat(cv.certificates())
				.containsExactly("AWS Certified Cloud Practitioner (CLF-C02), Amazon Web Services (AWS)");
		assertThat(cv.education()).containsExactly(
				"Bachelor of Science in Computer Science, Riverside Technical University (fictional), Sep 2015 – Jun 2019");
	}

}
