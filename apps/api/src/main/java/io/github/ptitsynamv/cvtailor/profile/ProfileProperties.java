package io.github.ptitsynamv.cvtailor.profile;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param dir root of a profile folder in the career-ops layout ({@code demo-profile} or {@code ../career-ops}).
 *            A String, not a Path: Spring converts Paths through the resource loader, and under a real servlet
 *            context it rejects relative paths like {@code ../../demo-profile}.
 */
@ConfigurationProperties("app.profile")
public record ProfileProperties(String dir) {
}
