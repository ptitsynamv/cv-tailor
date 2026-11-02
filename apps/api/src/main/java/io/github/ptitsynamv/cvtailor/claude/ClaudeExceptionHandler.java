package io.github.ptitsynamv.cvtailor.claude;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.anthropic.errors.AnthropicException;

/** Turns a failed Claude call into a 502 problem response, so the UI can show it as an error state. */
@RestControllerAdvice
class ClaudeExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ClaudeExceptionHandler.class);

	@ExceptionHandler(ClaudeException.class)
	ProblemDetail handleClaude(ClaudeException e) {
		log.warn("Claude call failed: {}", e.getMessage());
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, e.getMessage());
	}

	@ExceptionHandler(AnthropicException.class)
	ProblemDetail handleApi(AnthropicException e) {
		log.warn("Claude API error", e);
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, "The Claude API request failed: " + e.getMessage());
	}

}
