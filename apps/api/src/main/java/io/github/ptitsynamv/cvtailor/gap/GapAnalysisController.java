package io.github.ptitsynamv.cvtailor.gap;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.github.ptitsynamv.cvtailor.job.JobAnalysis;

@RestController
@RequestMapping("/api")
public class GapAnalysisController {

	private final GapAnalysisService service;

	public GapAnalysisController(GapAnalysisService service) {
		this.service = service;
	}

	/** Takes the {@link JobAnalysis} from the previous step, possibly edited by the user. */
	@PostMapping("/gap-analysis")
	public GapReport analyze(@RequestBody JobAnalysis job) {
		if (job.mustHave().isEmpty() && job.niceToHave().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The job analysis has no requirements to match");
		}
		return service.analyze(job);
	}

}
