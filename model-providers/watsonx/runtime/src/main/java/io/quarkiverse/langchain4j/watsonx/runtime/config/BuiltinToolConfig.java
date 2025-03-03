package io.quarkiverse.langchain4j.watsonx.runtime.config;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigDocDefault;
import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.WithDefault;

@ConfigGroup
public interface BuiltinToolConfig {

    /**
     * Whether the watsonx.ai rest client for built-in tools should log requests.
     */
    @ConfigDocDefault("false")
    @WithDefault("${quarkus.langchain4j.log-requests}")
    Optional<Boolean> logRequests();

    /**
     * Whether the watsonx.ai rest client for built-in tools should log responses
     */
    @ConfigDocDefault("false")
    @WithDefault("${quarkus.langchain4j.log-requests}")
    Optional<Boolean> logResponses();
}
