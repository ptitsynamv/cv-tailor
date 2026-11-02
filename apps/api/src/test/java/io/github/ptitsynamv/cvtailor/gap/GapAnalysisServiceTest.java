package io.github.ptitsynamv.cvtailor.gap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.github.ptitsynamv.cvtailor.claude.ClaudeRequest;
import io.github.ptitsynamv.cvtailor.claude.ClaudeService;
import io.github.ptitsynamv.cvtailor.job.JobAnalysis;
import io.github.ptitsynamv.cvtailor.job.Seniority;
import io.github.ptitsynamv.cvtailor.profile.ProfileLoader;
import io.github.ptitsynamv.cvtailor.profile.ProfileProperties;

class GapAnalysisServiceTest {

	private static final JobAnalysis JOB = new JobAnalysis("Senior Frontend Engineer", "Acme", Seniority.SENIOR,
			List.of("5+ years of Angular", "Java"), List.of("Node.js"), List.of("Angular"));

	private final ClaudeService claude = mock(ClaudeService.class);
	private final ProfileLoader profileLoader = new ProfileLoader(new ProfileProperties(Path.of("../../demo-profile")));
	private final GapAnalysisService service = new GapAnalysisService(claude, profileLoader);

	@Test
	void sendsTheRenderedProfileAndTheNumberedRequirements() {
		when(claude.generate(any(), eq(GapReport.class))).thenReturn(new GapReport(List.of()));

		service.analyze(JOB);

		var request = ArgumentCaptor.forClass(ClaudeRequest.class);
		verify(claude).generate(request.capture(), eq(GapReport.class));
		assertThat(request.getValue().step()).isEqualTo("gap-analysis");
		assertThat(request.getValue().profile()).isEqualTo(profileLoader.load().render());
		assertThat(request.getValue().userMessage()).contains("""
				<requirements>
				1. [must] 5+ years of Angular
				2. [must] Java
				3. [nice] Node.js
				</requirements>""");
		assertThat(request.getValue().instructions()).contains("Never invent experience");
	}

	@Test
	void runsClaudesAnswerThroughTheGuardrail() {
		when(claude.generate(any(), eq(GapReport.class))).thenReturn(new GapReport(List.of(
				new GapItem("5+ years of Angular", GapStatus.STRONG, List.of("skill-angular"), "6+ years."),
				new GapItem("Java", GapStatus.STRONG, List.of("exp-lumenledger-99"), "Invented."))));

		GapReport report = service.analyze(JOB);

		assertThat(report.items()).extracting(GapItem::status).containsExactly(GapStatus.STRONG, GapStatus.GAP);
	}

}
