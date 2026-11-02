package io.github.ptitsynamv.cvtailor.job;

import org.springframework.stereotype.Service;

import io.github.ptitsynamv.cvtailor.claude.ClaudeRequest;
import io.github.ptitsynamv.cvtailor.claude.ClaudeService;

@Service
public class JobAnalysisService {

	static final String STEP = "job-analysis";

	static final String INSTRUCTIONS = """
			You read job posts for a CV-tailoring tool. The job post is in the <job_post> tags.

			Extract only what the job post says. Don't add requirements, and don't infer skills the post doesn't name.

			- mustHave: what the post presents as required (requirements, "what we expect", "you have"). Keep the post's \
			wording, shortened when long. Put each skill, experience or condition in its own item, so each can be \
			matched separately; keep a list of alternatives ("React, Angular, or Vue") together as one item. Include \
			stated conditions such as years of experience, language level, location, time zone and office attendance.
			- niceToHave: what the post marks as optional ("nice to have", "bonus", "extra points"). Technologies \
			named only in the responsibilities, and never as a requirement, also go here, one per item.
			- Leave out benefits, perks, company culture and descriptions of the team.
			- If the post is not a job post, return an empty title and empty lists.
			""";

	private final ClaudeService claude;

	public JobAnalysisService(ClaudeService claude) {
		this.claude = claude;
	}

	public JobAnalysis analyze(String jobText) {
		var request = new ClaudeRequest(STEP, INSTRUCTIONS, null, "<job_post>\n" + jobText.strip() + "\n</job_post>");
		return claude.generate(request, JobAnalysis.class);
	}

}
