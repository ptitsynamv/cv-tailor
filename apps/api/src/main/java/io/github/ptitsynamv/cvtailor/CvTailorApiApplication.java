package io.github.ptitsynamv.cvtailor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CvTailorApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CvTailorApiApplication.class, args);
	}

}
