package io.github.ptitsynamv.cvtailor.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.ptitsynamv.cvtailor.claude.ClaudeService;
import io.github.ptitsynamv.cvtailor.health.HealthResponse.ClaudeMode;

@RestController
@RequestMapping("/api")
public class HealthController {

	private final String serviceName;

	private final ClaudeService claude;

	public HealthController(@Value("${spring.application.name}") String serviceName, ClaudeService claude) {
		this.serviceName = serviceName;
		this.claude = claude;
	}

	@GetMapping("/health")
	public HealthResponse health() {
		return new HealthResponse("UP", serviceName, claude.isFake() ? ClaudeMode.FAKE : ClaudeMode.LIVE);
	}

}
