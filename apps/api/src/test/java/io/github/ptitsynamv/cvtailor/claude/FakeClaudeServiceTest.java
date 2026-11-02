package io.github.ptitsynamv.cvtailor.claude;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.json.JsonMapper;

class FakeClaudeServiceTest {

	record Sample(String title, List<String> keywords) {
	}

	record WrongShape(int title) {
	}

	private final FakeClaudeService service = new FakeClaudeService(JsonMapper.builder().build());

	@Test
	void returnsTheStepFixtureAsTheRequestedRecord() {
		Sample result = service.generate(request("test-step"), Sample.class);

		assertThat(result).isEqualTo(new Sample("Senior Frontend Engineer", List.of("Angular", "TypeScript")));
	}

	@Test
	void namesTheMissingFixtureFile() {
		assertThatThrownBy(() -> service.generate(request("no-such-step"), Sample.class))
				.isInstanceOf(ClaudeException.class)
				.hasMessageContaining("src/main/resources/fake-claude/no-such-step.json");
	}

	@Test
	void reportsAFixtureThatDoesNotMatchTheRecord() {
		assertThatThrownBy(() -> service.generate(request("test-step"), WrongShape.class))
				.isInstanceOf(ClaudeException.class)
				.hasMessageContaining("doesn't match WrongShape");
	}

	private static ClaudeRequest request(String step) {
		return new ClaudeRequest(step, "instructions", null, "job text");
	}

}
