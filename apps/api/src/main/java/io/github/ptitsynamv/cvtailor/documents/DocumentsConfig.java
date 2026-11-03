package io.github.ptitsynamv.cvtailor.documents;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class DocumentsConfig {

	/** Dates the letter and the output folder; a bean so tests can fix it. */
	@Bean
	Clock clock() {
		return Clock.systemDefaultZone();
	}

}
