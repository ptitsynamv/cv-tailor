package io.github.ptitsynamv.cvtailor.profile;

import java.nio.file.Path;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param dir root of a profile folder in the career-ops layout ({@code demo-profile} or {@code ../career-ops})
 */
@ConfigurationProperties("app.profile")
public record ProfileProperties(Path dir) {
}
