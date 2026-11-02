package io.github.ptitsynamv.cvtailor.job;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.json.JsonMapper;

/** The whole app in fake mode (an empty key forces it even if one is set locally): a real sample job in, the fixture out. */
@SpringBootTest(properties = "ANTHROPIC_API_KEY=")
@AutoConfigureMockMvc
class JobAnalysisFakeModeTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JsonMapper jsonMapper;

	@Test
	void analyzesASampleJobWithTheFixture() throws Exception {
		String jobText = Files.readString(Path.of("../../docs/sample-jobs/wix-ai-native-xengineer-frontend.txt"));

		mockMvc.perform(post("/api/job-analysis").contentType(MediaType.APPLICATION_JSON)
				.content(jsonMapper.writeValueAsString(Map.of("jobText", jobText))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("AI-native xEngineer (with Frontend core)"))
				.andExpect(jsonPath("$.company").value("Wix"))
				.andExpect(jsonPath("$.seniority").value("SENIOR"))
				.andExpect(jsonPath("$.mustHave.length()").value(13))
				.andExpect(jsonPath("$.niceToHave[0]").value("React"));
	}

}
