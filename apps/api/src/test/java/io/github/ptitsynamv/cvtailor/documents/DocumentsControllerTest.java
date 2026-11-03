package io.github.ptitsynamv.cvtailor.documents;

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

import io.github.ptitsynamv.cvtailor.profile.Contact;

@WebMvcTest(DocumentsController.class)
class DocumentsControllerTest {

	private static final String JOB = """
			{"title": "Frontend Engineer", "company": "Acme", "seniority": "SENIOR",
			 "mustHave": ["Angular"], "niceToHave": [], "keywords": []}""";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private DocumentsService service;

	@Test
	void returnsBothDocumentsWithBase64Pdfs() throws Exception {
		var cv = new TailoredCv(Contact.EMPTY, List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
		when(service.generate(any(), any())).thenReturn(new DocumentsResponse(cv,
				new CoverLetter("Dear Hiring Team,", List.of("Hello.")), "alex-doe-acme", new byte[] { 1, 2, 3 },
				new byte[] { 4 }, null));

		mockMvc.perform(post("/api/documents").contentType(MediaType.APPLICATION_JSON).content("""
				{"job": %s, "gaps": {"items": [{"requirement": "Angular", "status": "STRONG",
				 "evidenceIds": ["skill-angular"], "note": "6+ years."}]}}""".formatted(JOB)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.coverLetter.greeting").value("Dear Hiring Team,"))
				.andExpect(jsonPath("$.cvPdf").value("AQID"))
				.andExpect(jsonPath("$.fileBaseName").value("alex-doe-acme"));
	}

	@Test
	void rejectsAMissingGapAnalysis() throws Exception {
		mockMvc.perform(post("/api/documents").contentType(MediaType.APPLICATION_JSON).content("{\"job\": " + JOB + "}"))
				.andExpect(status().isBadRequest());

		verifyNoInteractions(service);
	}

	@Test
	void rejectsAnEmptyGapAnalysis() throws Exception {
		mockMvc.perform(post("/api/documents").contentType(MediaType.APPLICATION_JSON)
				.content("{\"job\": " + JOB + ", \"gaps\": {\"items\": []}}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("The gap analysis is empty"));

		verifyNoInteractions(service);
	}

}
