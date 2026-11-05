package io.github.ptitsynamv.cvtailor.common;

import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Spring's standard problem JSON for MVC errors, except that a validation failure's {@code detail} carries the
 * field messages (e.g. "Paste the job post text") instead of a generic "Invalid request content.", so the UI can
 * show them. Defining this bean replaces the one {@code spring.mvc.problemdetails.enabled} would register.
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	@Override
	protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		String messages = ex.getBindingResult().getAllErrors().stream()
				.map(error -> error.getDefaultMessage() == null ? "Invalid value" : error.getDefaultMessage())
				.distinct()
				.collect(Collectors.joining("; "));
		ex.getBody().setDetail(messages);
		return handleExceptionInternal(ex, null, headers, status, request);
	}

}
