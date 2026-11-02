package io.github.ptitsynamv.cvtailor.gap;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The whole app in fake mode against demo-profile: the job-analysis fixture goes in, and the gap-analysis fixture
 * comes out through the guardrail, which catches the fixture's one invented evidence id.
 */
@SpringBootTest(properties = "ANTHROPIC_API_KEY=")
@AutoConfigureMockMvc
class GapAnalysisFakeModeTest {

	private static final String AI_TOOLS_ITEM = "$.items[5]";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void guardrailCatchesTheInventedIdInTheFixture() throws Exception {
		String jobAnalysis = new ClassPathResource("fake-claude/job-analysis.json").getContentAsString(java.nio.charset.StandardCharsets.UTF_8);

		mockMvc.perform(post("/api/gap-analysis").contentType(MediaType.APPLICATION_JSON).content(jobAnalysis))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items", hasSize(18)))
				.andExpect(jsonPath("$.items[0].status").value("STRONG"))
				.andExpect(jsonPath(AI_TOOLS_ITEM + ".requirement", startsWith("Experience using AI tools")))
				.andExpect(jsonPath(AI_TOOLS_ITEM + ".status").value("GAP"))
				.andExpect(jsonPath(AI_TOOLS_ITEM + ".evidenceIds", hasSize(0)))
				.andExpect(jsonPath(AI_TOOLS_ITEM + ".note", startsWith("Guardrail:")));
	}

}
