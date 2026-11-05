package io.github.ptitsynamv.cvtailor.health;

/**
 * @param mode {@code FAKE} when Claude answers are canned fixtures (no API key), {@code LIVE} otherwise
 */
public record HealthResponse(String status, String service, ClaudeMode mode) {

	public enum ClaudeMode {

		FAKE, LIVE

	}

}
