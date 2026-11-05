package io.github.ptitsynamv.cvtailor.claude;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/** Returns the hand-written fixture {@code fake-claude/<step>.json} for every request, ignoring its content. */
public class FakeClaudeService implements ClaudeService {

	static final String FIXTURE_DIR = "fake-claude/";

	private static final Logger log = LoggerFactory.getLogger(FakeClaudeService.class);

	private final JsonMapper jsonMapper;

	public FakeClaudeService(JsonMapper jsonMapper) {
		this.jsonMapper = jsonMapper;
		log.warn("ANTHROPIC_API_KEY is not set: FAKE mode. Claude calls return canned fixtures from {}", FIXTURE_DIR);
	}

	@Override
	public boolean isFake() {
		return true;
	}

	@Override
	public <T> T generate(ClaudeRequest request, Class<T> outputType) {
		String path = FIXTURE_DIR + request.step() + ".json";
		var fixture = new ClassPathResource(path);
		if (!fixture.exists()) {
			throw new ClaudeException("No fake fixture for step '%s': add src/main/resources/%s".formatted(request.step(), path));
		}
		try (InputStream in = fixture.getInputStream()) {
			return jsonMapper.readValue(in, outputType);
		}
		catch (IOException | JacksonException e) {
			throw new ClaudeException("Fake fixture %s doesn't match %s".formatted(path, outputType.getSimpleName()), e);
		}
	}

}
