package io.github.ptitsynamv.cvtailor.gap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GapAnalysisController.class)
class GapAnalysisControllerTest {

	private static final String JOB_JSON = """
			{"title": "Senior Frontend Engineer", "company": "Acme", "seniority": "SENIOR",
			 "mustHave": [%s], "niceToHave": [], "keywords": []}""";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private GapAnalysisService service;

	@Test
	void returnsTheGapReport() throws Exception {
		when(service.analyze(any())).thenReturn(new GapReport(List.of(
				new GapItem("Angular", GapStatus.STRONG, List.of("skill-angular"), "6+ years."))));

		mockMvc.perform(post("/api/gap-analysis").contentType(MediaType.APPLICATION_JSON)
				.content(JOB_JSON.formatted("\"Angular\"")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].status").value("STRONG"))
				.andExpect(jsonPath("$.items[0].evidenceIds[0]").value("skill-angular"));
	}

	@Test
	void rejectsAJobAnalysisWithoutRequirements() throws Exception {
		mockMvc.perform(post("/api/gap-analysis").contentType(MediaType.APPLICATION_JSON).content(JOB_JSON.formatted("")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("The job analysis has no requirements to match"));

		verifyNoInteractions(service);
	}

	@Test
	void rejectsAMalformedBody() throws Exception {
		mockMvc.perform(post("/api/gap-analysis").contentType(MediaType.APPLICATION_JSON).content("{\"title\": \"x\"}"))
				.andExpect(status().isBadRequest());

		verifyNoInteractions(service);
	}

}
