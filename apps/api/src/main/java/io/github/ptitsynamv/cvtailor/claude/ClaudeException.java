package io.github.ptitsynamv.cvtailor.claude;

public class ClaudeException extends RuntimeException {

	public ClaudeException(String message) {
		super(message);
	}

	public ClaudeException(String message, Throwable cause) {
		super(message, cause);
	}

}
