package io.github.ptitsynamv.cvtailor.claude;

import org.jspecify.annotations.Nullable;

/**
 * @param step         pipeline step name, e.g. {@code job-analysis}; the fake service loads {@code fake-claude/<step>.json}
 * @param instructions system prompt for this step
 * @param profile      rendered profile, sent before the instructions and marked for prompt caching; null if the step
 *                     doesn't need it
 * @param userMessage  the per-request input, e.g. the job post text
 */
public record ClaudeRequest(String step, String instructions, @Nullable String profile, String userMessage) {
}
