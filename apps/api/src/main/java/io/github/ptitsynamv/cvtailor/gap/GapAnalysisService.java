package io.github.ptitsynamv.cvtailor.gap;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

import io.github.ptitsynamv.cvtailor.claude.ClaudeRequest;
import io.github.ptitsynamv.cvtailor.claude.ClaudeService;
import io.github.ptitsynamv.cvtailor.job.JobAnalysis;
import io.github.ptitsynamv.cvtailor.profile.Profile;
import io.github.ptitsynamv.cvtailor.profile.ProfileLoader;

@Service
public class GapAnalysisService {

	static final String STEP = "gap-analysis";

	static final String INSTRUCTIONS = """
			You compare a job's requirements with the candidate profile above, for a CV-tailoring tool. Each \
			evidence line in the profile starts with its id in [brackets]. The requirements are in the \
			<requirements> tags.

			Return one item per requirement, in the given order, with the requirement text unchanged.

			- STRONG: the profile shows the requirement directly, at the level asked (years, seniority, scope).
			- PARTIAL: related or weaker evidence: an adjacent technology, less experience than asked, or only a side \
			project or certification where the job expects professional use.
			- GAP: nothing in the profile supports it.

			A STRONG or PARTIAL match must cite the ids of the lines that support it, copied exactly. Anything \
			without evidence is a GAP. Never invent experience, and don't infer a skill from a job title or a \
			company's industry. Skills listed under "Not evidenced" are always a GAP.

			For conditions the profile can't show, such as location, office attendance or availability, use GAP \
			and say in the note that the candidate should confirm it.
			""";

	private final ClaudeService claude;
	private final ProfileLoader profileLoader;

	public GapAnalysisService(ClaudeService claude, ProfileLoader profileLoader) {
		this.claude = claude;
		this.profileLoader = profileLoader;
	}

	public GapReport analyze(JobAnalysis job) {
		Profile profile = profileLoader.load();
		var request = new ClaudeRequest(STEP, INSTRUCTIONS, profile.render(), userMessage(job));
		GapReport report = claude.generate(request, GapReport.class);
		return EvidenceGuardrail.verify(report, profile.ids());
	}

	static String userMessage(JobAnalysis job) {
		List<String> requirements = new ArrayList<>();
		job.mustHave().forEach(requirement -> requirements.add("[must] " + requirement));
		job.niceToHave().forEach(requirement -> requirements.add("[nice] " + requirement));
		String numbered = IntStream.range(0, requirements.size())
				.mapToObj(i -> (i + 1) + ". " + requirements.get(i))
				.collect(Collectors.joining("\n"));
		return """
				<job>
				Title: %s
				Company: %s
				Seniority: %s
				</job>
				<requirements>
				%s
				</requirements>""".formatted(job.title(), job.company(), job.seniority(), numbered);
	}

}
