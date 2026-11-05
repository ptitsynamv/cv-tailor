package io.github.ptitsynamv.cvtailor.job;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import io.github.ptitsynamv.cvtailor.claude.ClaudeException;
import io.github.ptitsynamv.cvtailor.claude.ClaudeService;

@WebMvcTest(JobAnalysisController.class)
@Import(JobAnalysisService.class)
class JobAnalysisControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ClaudeService claude;

	@Test
	void returnsTheJobAnalysis() throws Exception {
		when(claude.generate(any(), eq(JobAnalysis.class))).thenReturn(new JobAnalysis("Senior Frontend Engineer",
				"Acme", Seniority.SENIOR, List.of("5+ years of Angular"), List.of("Node.js"), List.of("Angular")));

		mockMvc.perform(post("/api/job-analysis").contentType(MediaType.APPLICATION_JSON)
				.content("{\"jobText\": \"Senior Frontend Engineer at Acme\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Senior Frontend Engineer"))
				.andExpect(jsonPath("$.seniority").value("SENIOR"))
				.andExpect(jsonPath("$.mustHave[0]").value("5+ years of Angular"));
	}

	@Test
	void rejectsBlankJobText() throws Exception {
		mockMvc.perform(post("/api/job-analysis").contentType(MediaType.APPLICATION_JSON).content("{\"jobText\": \"  \"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("Paste the job post text"));

		verifyNoInteractions(claude);
	}

	@Test
	void rejectsJobTextOverTheLimit() throws Exception {
		String tooLong = "a".repeat(JobAnalysisRequest.MAX_LENGTH + 1);

		mockMvc.perform(post("/api/job-analysis").contentType(MediaType.APPLICATION_JSON)
				.content("{\"jobText\": \"" + tooLong + "\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("Job post is too long (max 20000 characters)"));

		verifyNoInteractions(claude);
	}

	@Test
	void reportsAFailedClaudeCallAsBadGateway() throws Exception {
		when(claude.generate(any(), eq(JobAnalysis.class)))
				.thenThrow(new ClaudeException("Claude stopped with max_tokens at step 'job-analysis'"));

		mockMvc.perform(post("/api/job-analysis").contentType(MediaType.APPLICATION_JSON)
				.content("{\"jobText\": \"Senior Frontend Engineer\"}"))
				.andExpect(status().isBadGateway())
				.andExpect(jsonPath("$.detail").value("Claude stopped with max_tokens at step 'job-analysis'"));
	}

}
