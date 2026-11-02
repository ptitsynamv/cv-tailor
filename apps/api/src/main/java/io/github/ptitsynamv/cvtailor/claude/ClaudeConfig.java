package io.github.ptitsynamv.cvtailor.claude;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;

import tools.jackson.databind.json.JsonMapper;

/**
 * Picks the {@link ClaudeService}: the real one when {@code ANTHROPIC_API_KEY} is set, otherwise the fake one,
 * so the app always starts. Custom conditions (rather than {@code @ConditionalOnProperty}) treat an empty key as
 * missing.
 */
@Configuration(proxyBeanMethods = false)
class ClaudeConfig {

	static final String API_KEY = "ANTHROPIC_API_KEY";

	@Bean
	@Conditional(ApiKeyPresent.class)
	AnthropicClient anthropicClient(Environment environment) {
		return AnthropicOkHttpClient.builder().fromEnv().apiKey(environment.getRequiredProperty(API_KEY)).build();
	}

	@Bean
	@Conditional(ApiKeyPresent.class)
	ClaudeService anthropicClaudeService(AnthropicClient client, ClaudeProperties properties) {
		return new AnthropicClaudeService(client, properties);
	}

	@Bean
	@Conditional(ApiKeyMissing.class)
	ClaudeService fakeClaudeService(JsonMapper jsonMapper) {
		return new FakeClaudeService(jsonMapper);
	}

	static class ApiKeyPresent implements Condition {

		@Override
		public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
			return StringUtils.hasText(context.getEnvironment().getProperty(API_KEY));
		}

	}

	static class ApiKeyMissing implements Condition {

		@Override
		public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
			return !StringUtils.hasText(context.getEnvironment().getProperty(API_KEY));
		}

	}

}
