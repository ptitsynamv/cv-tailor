package io.github.ptitsynamv.cvtailor.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.github.ptitsynamv.cvtailor.claude.ClaudeRequest;
import io.github.ptitsynamv.cvtailor.claude.ClaudeService;

class JobAnalysisServiceTest {

	private final ClaudeService claude = mock(ClaudeService.class);
	private final JobAnalysisService service = new JobAnalysisService(claude);

	@Test
	void sendsTheJobPostInTagsWithoutTheProfile() {
		service.analyze("  Senior Frontend Engineer\nAngular required  ");

		var request = ArgumentCaptor.forClass(ClaudeRequest.class);
		verify(claude).generate(request.capture(), eq(JobAnalysis.class));
		assertThat(request.getValue().step()).isEqualTo("job-analysis");
		assertThat(request.getValue().profile()).isNull();
		assertThat(request.getValue().userMessage())
				.isEqualTo("<job_post>\nSenior Frontend Engineer\nAngular required\n</job_post>");
		assertThat(request.getValue().instructions())
				.contains("Extract only what the job post says. Don't add requirements");
	}

}
