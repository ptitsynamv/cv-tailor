package io.github.ptitsynamv.cvtailor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import io.github.ptitsynamv.cvtailor.profile.ProfileLoader;

/**
 * Starts the app on a real embedded server, as bootRun does. A mock servlet environment binds configuration
 * differently and once hid a startup failure here.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = "ANTHROPIC_API_KEY=")
class CvTailorApiApplicationTests {

	@Autowired
	private ProfileLoader profileLoader;

	@Test
	void startsAndLoadsTheDefaultProfile() {
		assertThat(profileLoader.load().items()).isNotEmpty();
	}

}
