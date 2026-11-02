package io.github.ptitsynamv.cvtailor.job;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class JobAnalysisController {

	private final JobAnalysisService service;

	public JobAnalysisController(JobAnalysisService service) {
		this.service = service;
	}

	@PostMapping("/job-analysis")
	public JobAnalysis analyze(@Validated @RequestBody JobAnalysisRequest request) {
		return service.analyze(request.jobText());
	}

}
