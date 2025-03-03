package io.quarkiverse.langchain4j.watsonx.runtime.config;

import java.time.Duration;
import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigDocDefault;
import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.WithDefault;

@ConfigGroup
public interface BuiltinToolConfig {

    /**
     * Base URL for the built-in tools.
     * <p>
     * If empty, the URL is automatically inherited from the {@code watsonx.wx-base-url} value or, if empty, calculated based on
     * the {@code watsonx.base-url} value.
     */
    Optional<String> baseUrl();

    /**
     * IBM Cloud API key.
     * <p>
     * If empty, the api key inherits the value from the {@code watsonx.api-key} property.
     */
    Optional<String> apiKey();

    /**
     * Timeout for built-in tools APIs.
     * <p>
     * If empty, the api key inherits the value from the {@code watsonx.timeout} property.
     */
    @ConfigDocDefault("10s")
    Optional<Duration> timeout();

    /**
     * Whether the built-in rest client should log requests.
     */
    @ConfigDocDefault("false")
    @WithDefault("${quarkus.langchain4j.log-requests}")
    Optional<Boolean> logRequests();

    /**
     * Whether the built-in rest client should log responses
     */
    @ConfigDocDefault("false")
    @WithDefault("${quarkus.langchain4j.log-requests}")
    Optional<Boolean> logResponses();
}
