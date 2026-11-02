package io.github.ptitsynamv.cvtailor.claude;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.claude")
public record ClaudeProperties(String model, long maxTokens) {
}
