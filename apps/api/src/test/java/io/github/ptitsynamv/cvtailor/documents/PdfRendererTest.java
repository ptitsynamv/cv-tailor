package io.github.ptitsynamv.cvtailor.documents;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import io.github.ptitsynamv.cvtailor.job.JobAnalysis;
import io.github.ptitsynamv.cvtailor.job.Seniority;
import io.github.ptitsynamv.cvtailor.profile.Contact;

class PdfRendererTest {

	// Long enough to wrap, as in demo-profile.
	private static final Contact CONTACT = new Contact("Alex Doe", "Senior Frontend Engineer", "Remote, EU",
			"alex.doe@example.com", "+00 000 000 000", List.of("linkedin.com/in/alex-doe-demo", "github.com/alex-doe-demo"));

	private static final TailoredCv CV = new TailoredCv(CONTACT,
			List.of(new CvBullet("Senior Frontend Engineer with 6+ years of Angular.", List.of("skill-angular"))),
			List.of(new CvSkillGroup("Frontend", List.of("Angular", "TypeScript"), List.of("skill-angular"))),
			List.of(new TailoredCv.Role("exp-a", "Senior Frontend Engineer", "Lumen Ledger", "Apr 2022 – Present",
					"Remote, EU", List.of(new CvBullet("Upgraded Angular 15 → 19 for the team’s app.", List.of("exp-a-1"))))),
			List.of(), List.of("AWS Certified Cloud Practitioner, AWS"), List.of("B.Sc. in Computer Science"));

	private final PdfRenderer renderer = new PdfRenderer(templateEngine());

	@Test
	void rendersTheCvAsAPdfWithAtsSectionNames() throws IOException {
		byte[] pdf = renderer.render("pdf/cv", Map.of("cv", CV));

		assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");
		String text = text(pdf);
		// Headings are capitalized by CSS, and the PDF text carries the capitals, which ATS parsers read fine.
		assertThat(text).contains("Alex Doe", "SUMMARY", "SKILLS", "EXPERIENCE", "CERTIFICATIONS", "EDUCATION")
				.contains("Senior Frontend Engineer, Lumen Ledger")
				.contains("alex.doe@example.com");
	}

	@Test
	void neverBreaksAContactLinkAcrossLines() throws IOException {
		String text = text(renderer.render("pdf/cv", Map.of("cv", CV)));

		assertThat(text).contains("linkedin.com/in/alex-doe-demo", "github.com/alex-doe-demo");
	}

	@Test
	void embedsAFontThatShowsDashesQuotesAndArrows() throws IOException {
		String text = text(renderer.render("pdf/cv", Map.of("cv", CV)));

		assertThat(text).contains("Apr 2022 – Present").contains("Angular 15 → 19").contains("team’s");
	}

	@Test
	void rendersTheCoverLetterWithSignOffFromCode() throws IOException {
		var job = new JobAnalysis("Frontend Engineer", "Acme", Seniority.SENIOR, List.of(), List.of(), List.of());
		var letter = new CoverLetter("Dear Hiring Team,", List.of("First paragraph.", "Second paragraph."));

		String text = text(renderer.render("pdf/cover-letter",
				Map.of("contact", CONTACT, "letter", letter, "job", job, "date", "3 November 2026")));

		assertThat(text).contains("Re: Frontend Engineer at Acme", "Dear Hiring Team,", "Second paragraph.",
				"Kind regards,", "3 November 2026");
	}

	private static String text(byte[] pdf) throws IOException {
		try (PDDocument document = Loader.loadPDF(pdf)) {
			return new PDFTextStripper().getText(document);
		}
	}

	private static SpringTemplateEngine templateEngine() {
		var resolver = new ClassLoaderTemplateResolver();
		resolver.setPrefix("templates/");
		resolver.setSuffix(".html");
		resolver.setTemplateMode(TemplateMode.HTML);
		resolver.setCharacterEncoding("UTF-8");
		var engine = new SpringTemplateEngine();
		engine.setTemplateResolver(resolver);
		return engine;
	}

}
