package io.github.ptitsynamv.cvtailor.claude;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.anthropic.client.AnthropicClient;

import tools.jackson.databind.json.JsonMapper;

class ClaudeConfigTest {

	@EnableConfigurationProperties(ClaudeProperties.class)
	static class PropertiesConfig {
	}

	private final ApplicationContextRunner runner = new ApplicationContextRunner()
			.withUserConfiguration(ClaudeConfig.class, PropertiesConfig.class)
			.withBean(JsonMapper.class, () -> JsonMapper.builder().build())
			.withPropertyValues("app.claude.model=claude-opus-5-5", "app.claude.max-tokens=16000");

	@Test
	void usesTheFakeServiceWithoutAnApiKey() {
		runner.withPropertyValues("ANTHROPIC_API_KEY=").run(context -> {
			assertThat(context).getBean(ClaudeService.class).isInstanceOf(FakeClaudeService.class);
			assertThat(context).doesNotHaveBean(AnthropicClient.class);
		});
	}

	@Test
	void usesTheRealServiceWhenTheApiKeyIsSet() {
		runner.withPropertyValues("ANTHROPIC_API_KEY=test-key").run(context -> {
			assertThat(context).getBean(ClaudeService.class).isInstanceOf(AnthropicClaudeService.class);
			assertThat(context).hasSingleBean(AnthropicClient.class);
		});
	}

}
