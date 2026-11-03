package io.github.ptitsynamv.cvtailor.documents;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class DocumentsController {

	private final DocumentsService service;

	public DocumentsController(DocumentsService service) {
		this.service = service;
	}

	@PostMapping("/documents")
	public DocumentsResponse generate(@Validated @RequestBody DocumentsRequest request) {
		if (request.gaps().items().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The gap analysis is empty");
		}
		return service.generate(request.job(), request.gaps());
	}

}
