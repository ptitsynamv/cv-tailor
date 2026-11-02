package io.github.ptitsynamv.cvtailor.claude;

/**
 * The app's only way to call Claude. {@link AnthropicClaudeService} calls the API;
 * {@link FakeClaudeService} returns canned fixtures when no API key is set.
 */
public interface ClaudeService {

	/** Sends one request and returns the answer parsed into {@code outputType} via structured outputs. */
	<T> T generate(ClaudeRequest request, Class<T> outputType);

}
