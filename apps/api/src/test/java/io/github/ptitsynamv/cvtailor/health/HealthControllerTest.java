package io.github.ptitsynamv.cvtailor.health;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import io.github.ptitsynamv.cvtailor.claude.ClaudeService;

@WebMvcTest(HealthController.class)
class HealthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ClaudeService claude;

	@Test
	void healthReturnsUpWithServiceName() throws Exception {
		mockMvc.perform(get("/api/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"))
				.andExpect(jsonPath("$.service").value("cv-tailor-api"))
				.andExpect(jsonPath("$.mode").value("LIVE"));
	}

	@Test
	void healthReportsFakeModeSoTheUiCanShowADemoBanner() throws Exception {
		when(claude.isFake()).thenReturn(true);

		mockMvc.perform(get("/api/health")).andExpect(jsonPath("$.mode").value("FAKE"));
	}

}
