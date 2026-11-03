package io.github.ptitsynamv.cvtailor.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

class ProfileLoaderTest {

	private static final String DEMO_PROFILE = "../../demo-profile";

	private final Profile profile = new ProfileLoader(new ProfileProperties(DEMO_PROFILE)).load();

	@Test
	void loadsEverySectionOfTheDemoProfile() {
		assertThat(profile.ids()).contains(
				"exp-lumenledger", "exp-lumenledger-1", "exp-brightpathlogistics",
				"proj-budget-buddy", "proj-route-notes-api",
				"skill-angular", "skill-node-js-express",
				"cert-aws-certified-cloud-practitioner-clf-c02",
				"edu-bachelors-degree");
		for (ProfileSection section : ProfileSection.values()) {
			assertThat(profile.items()).as(section.name()).anyMatch(item -> item.section() == section);
		}
	}

	@Test
	void givesEveryItemAUniqueId() {
		List<String> ids = profile.items().stream().map(ProfileItem::id).toList();

		assertThat(ids).doesNotHaveDuplicates();
	}

	@Test
	void numbersExperienceBulletsWithTheirSectionLabel() {
		ProfileItem role = item("exp-lumenledger");
		ProfileItem firstBullet = item("exp-lumenledger-1");

		assertThat(role.text()).startsWith("Company: Lumen Ledger | Role / Title: Senior Frontend Engineer");
		assertThat(firstBullet.text())
				.isEqualTo("Executive Summary & Core Responsibilities: Lead the frontend of a B2B invoicing platform "
						+ "used by small and mid-size companies to create, send and track invoices.");
	}

	@Test
	void turnsTableRowsIntoHeaderValuePairs() {
		assertThat(item("proj-budget-buddy").text())
				.startsWith("Project Name: budget-buddy | Primary Stack: Angular 20, Signals, Tailwind |");
	}

	@Test
	void keepsNotEvidencedSkillsOutOfTheCitableItems() {
		assertThat(profile.doNotClaim()).contains("GraphQL", "Kubernetes");
		assertThat(profile.items()).noneMatch(item -> item.text().contains("GraphQL"));
	}

	@Test
	void rendersOneLinePerItemWithIdMarkers() {
		String text = profile.render();

		assertThat(text).contains("\n## Experience\n[exp-brightpathlogistics] Company: Brightpath Logistics")
				.contains("\n[exp-lumenledger] Company: Lumen Ledger")
				.contains("\n[proj-budget-buddy] Project Name: budget-buddy")
				.contains("\n## Not evidenced (never claim these)\n- React or Next.js in production\n")
				.doesNotContain("**")
				.doesNotContain("`");
	}

	@Test
	void rendersTheSameTextOnEveryLoadSoThePromptCacheHits() {
		Profile reloaded = new ProfileLoader(new ProfileProperties(DEMO_PROFILE)).load();

		assertThat(reloaded.render()).isEqualTo(profile.render());
	}

	@Test
	void failsClearlyWhenTheProfileFolderIsMissing() {
		var loader = new ProfileLoader(new ProfileProperties("no-such-profile"));

		assertThatThrownBy(loader::load).isInstanceOf(IllegalStateException.class).hasMessageContaining("PROFILE_DIR");
	}

	private ProfileItem item(String id) {
		return profile.items().stream().filter(item -> item.id().equals(id)).findFirst().orElseThrow();
	}

}
