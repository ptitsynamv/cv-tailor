package io.github.ptitsynamv.cvtailor.documents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.json.JsonMapper;

/** The whole app in fake mode: fixtures for the previous steps in, two PDFs out and saved to OUTPUT_DIR. */
@SpringBootTest(properties = "ANTHROPIC_API_KEY=")
@AutoConfigureMockMvc
class DocumentsFakeModeTest {

	@TempDir
	private static Path outputDir;

	@DynamicPropertySource
	static void outputDir(DynamicPropertyRegistry registry) {
		registry.add("app.documents.output-dir", outputDir::toString);
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JsonMapper jsonMapper;

	@Test
	void generatesAndSavesBothPdfs() throws Exception {
		String body = "{\"job\": " + fixture("job-analysis") + ", \"gaps\": " + fixture("gap-analysis") + "}";

		String json = mockMvc.perform(post("/api/documents").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
		DocumentsResponse response = jsonMapper.readValue(json, DocumentsResponse.class);

		assertThat(response.cv().contact().name()).isEqualTo("Alex Doe");
		assertThat(response.cv().experience()).extracting(TailoredCv.Role::company)
				.containsExactly("Lumen Ledger", "Brightpath Logistics");
		assertThat(response.cv().experience().getFirst().bullets()).hasSize(6);
		assertThat(response.coverLetter().wordCount()).isLessThanOrEqualTo(DocumentsService.MAX_LETTER_WORDS);
		assertThat(response.fileBaseName()).isEqualTo("alex-doe-wix");
		assertThat(new String(response.cvPdf(), 0, 5)).isEqualTo("%PDF-");

		Path folder = outputDir.resolve(LocalDate.now() + "-wix-ai-native-xengineer-with-frontend-core");
		assertThat(response.savedTo()).isEqualTo(folder.toAbsolutePath().normalize().toString());
		assertThat(Files.readAllBytes(folder.resolve("cv.pdf"))).isEqualTo(response.cvPdf());
		assertThat(Files.readAllBytes(folder.resolve("cover-letter.pdf"))).isEqualTo(response.coverLetterPdf());
	}

	private static String fixture(String step) throws Exception {
		return new ClassPathResource("fake-claude/" + step + ".json").getContentAsString(StandardCharsets.UTF_8);
	}

}
