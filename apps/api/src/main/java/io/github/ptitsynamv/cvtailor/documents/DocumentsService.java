package io.github.ptitsynamv.cvtailor.documents;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.github.ptitsynamv.cvtailor.claude.ClaudeRequest;
import io.github.ptitsynamv.cvtailor.claude.ClaudeService;
import io.github.ptitsynamv.cvtailor.common.Slug;
import io.github.ptitsynamv.cvtailor.gap.GapReport;
import io.github.ptitsynamv.cvtailor.job.JobAnalysis;
import io.github.ptitsynamv.cvtailor.profile.Profile;
import io.github.ptitsynamv.cvtailor.profile.ProfileLoader;

@Service
public class DocumentsService {

	static final String STEP = "documents";
	static final int MAX_LETTER_WORDS = 250;

	static final String INSTRUCTIONS = """
			You write a tailored CV and cover letter for the job below, for a CV-tailoring tool. Use the candidate \
			profile above: each evidence line starts with its id in [brackets]. The job is in the <job> tags and \
			the gap analysis in the <gap_analysis> tags.

			Rules:
			- Use only facts from the profile. Reword, but never inflate: no bigger numbers, titles, scope or years \
			than the profile states, and nothing listed under "Not evidenced".
			- Every summary sentence, skill group and bullet cites the ids of the lines it's based on, copied \
			exactly. The tool removes anything without a valid id.
			- Lead with what the job asks for. Present PARTIAL matches honestly (for example "side project"). \
			Leave GAP items out of the CV.

			CV:
			- summary: 2-3 sentences for this job.
			- skills: 3-5 groups with ATS-friendly names (e.g. Frontend, Testing), using the job's terms where the \
			profile supports them.
			- experience: every role in the profile, most recent first, referenced by its role id (the exp- id \
			without a number). 3-5 bullets for recent roles, 2-3 for older ones, each starting with a strong verb.
			- projects: 0-2 projects relevant to the job, by their proj- id.
			- The CV must fit 1-2 pages. The tool adds contact details, role titles, dates, certificates and \
			education, so don't write them.

			Cover letter:
			- At most 250 words in 3-4 paragraphs. Greeting "Dear Hiring Team," unless the post names a person.
			- Use the same facts as the CV. Where a GAP matters for the job, address it honestly without claiming \
			it.
			- No sign-off and no candidate name; the tool adds them.
			""";

	private static final Logger log = LoggerFactory.getLogger(DocumentsService.class);
	private static final DateTimeFormatter LETTER_DATE = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH);

	private final ClaudeService claude;
	private final ProfileLoader profileLoader;
	private final PdfRenderer pdfRenderer;
	private final DocumentStorage storage;
	private final Clock clock;

	public DocumentsService(ClaudeService claude, ProfileLoader profileLoader, PdfRenderer pdfRenderer,
			DocumentStorage storage, Clock clock) {
		this.claude = claude;
		this.profileLoader = profileLoader;
		this.pdfRenderer = pdfRenderer;
		this.storage = storage;
		this.clock = clock;
	}

	public DocumentsResponse generate(JobAnalysis job, GapReport gaps) {
		Profile profile = profileLoader.load();
		var request = new ClaudeRequest(STEP, INSTRUCTIONS, profile.render(), userMessage(job, gaps));
		DocumentsDraft draft = claude.generate(request, DocumentsDraft.class);

		TailoredCv cv = CvAssembler.assemble(CvGuardrail.verify(draft.cv(), profile), profile);
		CoverLetter letter = draft.coverLetter();
		if (letter.wordCount() > MAX_LETTER_WORDS) {
			log.warn("Cover letter has {} words, over the {} limit", letter.wordCount(), MAX_LETTER_WORDS);
		}

		LocalDate today = LocalDate.now(clock);
		byte[] cvPdf = pdfRenderer.render("pdf/cv", Map.of("cv", cv));
		byte[] letterPdf = pdfRenderer.render("pdf/cover-letter", Map.of("contact", profile.contact(), "letter",
				letter, "job", job, "date", LETTER_DATE.format(today)));

		String company = job.company().isBlank() ? "company" : job.company();
		Map<String, byte[]> files = new LinkedHashMap<>();
		files.put("cv.pdf", cvPdf);
		files.put("cover-letter.pdf", letterPdf);
		String savedTo = storage.save(today + "-" + Slug.of(company) + "-" + Slug.of(job.title()), files)
				.map(Object::toString)
				.orElse(null);

		String fileBaseName = Slug.of(profile.contact().name() + " " + company);
		return new DocumentsResponse(cv, letter, fileBaseName, cvPdf, letterPdf, savedTo);
	}

	static String userMessage(JobAnalysis job, GapReport gaps) {
		String gapLines = gaps.items().stream()
				.map(item -> "- [%s] %s (evidence: %s) %s".formatted(item.status(), item.requirement(),
						item.evidenceIds().isEmpty() ? "none" : String.join(", ", item.evidenceIds()), item.note()))
				.collect(Collectors.joining("\n"));
		return """
				<job>
				Title: %s
				Company: %s
				Seniority: %s
				Keywords: %s
				</job>
				<gap_analysis>
				%s
				</gap_analysis>""".formatted(job.title(), job.company(), job.seniority(),
				String.join(", ", job.keywords()), gapLines);
	}

}
