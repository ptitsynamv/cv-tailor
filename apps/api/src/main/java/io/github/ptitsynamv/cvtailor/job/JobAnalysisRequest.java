package io.github.ptitsynamv.cvtailor.job;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** A pasted job post. The size cap keeps requests cheap; real posts are 2–8K characters. */
public record JobAnalysisRequest(
		@NotBlank(message = "Paste the job post text") @Size(max = JobAnalysisRequest.MAX_LENGTH, message = "Job post is too long (max {max} characters)") String jobText) {

	public static final int MAX_LENGTH = 20_000;

}
