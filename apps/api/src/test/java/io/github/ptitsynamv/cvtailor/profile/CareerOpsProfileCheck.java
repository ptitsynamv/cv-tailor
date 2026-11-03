package io.github.ptitsynamv.cvtailor.profile;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * Manual check against the private profile; skipped unless CAREER_OPS_DIR is set. Prints only counts and ids,
 * never profile text:
 * {@code CAREER_OPS_DIR=../../../career-ops ./gradlew test --tests CareerOpsProfileCheck -i}
 */
@EnabledIfEnvironmentVariable(named = "CAREER_OPS_DIR", matches = ".+")
class CareerOpsProfileCheck {

	@Test
	void loadsTheRealProfile() {
		Profile profile = new ProfileLoader(new ProfileProperties(System.getenv("CAREER_OPS_DIR"))).load();

		Map<ProfileSection, Long> counts = Arrays.stream(ProfileSection.values())
				.collect(Collectors.toMap(Function.identity(),
						section -> profile.items().stream().filter(item -> item.section() == section).count()));
		System.out.println("Items per section: " + counts);
		System.out.println("Do-not-claim entries: " + profile.doNotClaim().size());
		System.out.println("Rendered length: " + profile.render().length() + " chars");
		System.out.println("Ids: " + profile.items().stream().map(ProfileItem::id).toList());

		assertThat(counts.values()).allMatch(count -> count > 0);
	}

}
