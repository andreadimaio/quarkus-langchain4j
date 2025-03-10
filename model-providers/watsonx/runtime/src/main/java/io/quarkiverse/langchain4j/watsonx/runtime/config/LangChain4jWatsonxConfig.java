package io.quarkiverse.langchain4j.watsonx.runtime.config;

import static io.quarkus.runtime.annotations.ConfigPhase.RUN_TIME;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigDocDefault;
import io.quarkus.runtime.annotations.ConfigDocMapKey;
import io.quarkus.runtime.annotations.ConfigDocSection;
import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithDefaults;
import io.smallrye.config.WithParentName;

@ConfigRoot(phase = RUN_TIME)
@ConfigMapping(prefix = "quarkus.langchain4j.watsonx")
public interface LangChain4jWatsonxConfig {

    /**
     * Default model config.
     */
    @WithParentName
    WatsonConfig defaultConfig();

    /**
     * Named model config.
     */
    @ConfigDocSection
    @ConfigDocMapKey("model-name")
    @WithParentName
    @WithDefaults
    Map<String, WatsonConfig> namedConfig();

    /**
     * Configuration for built-in tools.
     */
    BuiltinToolConfig builtinTool();

    @ConfigGroup
    interface WatsonConfig {
        /**
         * Base URL of the watsonx.ai API.
         */
        Optional<String> baseUrl();

        /**
         * Base URL for the built-in tools.
         * <p>
         * If empty, this URL will be automatically calculated based on the {@code base-url} property.
         */
        Optional<String> wxBaseUrl();

        /**
         * IBM Cloud API key.
         */
        Optional<String> apiKey();

        /**
         * Timeout for watsonx.ai calls.
         */
        @ConfigDocDefault("10s")
        @WithDefault("${quarkus.langchain4j.timeout}")
        Optional<Duration> timeout();

        /**
         * The version date for the API of the form YYYY-MM-DD.
         */
        @WithDefault("2024-03-14")
        String version();

        /**
         * The space that contains the resource.
         * <p>
         * Either <code>space_id</code> or <code>project_id</code> has to be given.
         */
        Optional<String> spaceId();

        /**
         * The project that contains the resource.
         * <p>
         * Either <code>space_id</code> or <code>project_id</code> has to be given.
         */
        Optional<String> projectId();

        /**
         * Whether the watsonx.ai client should log requests.
         */
        @ConfigDocDefault("false")
        @WithDefault("${quarkus.langchain4j.log-requests}")
        Optional<Boolean> logRequests();

        /**
         * Whether the watsonx.ai client should log responses.
         */
        @ConfigDocDefault("false")
        @WithDefault("${quarkus.langchain4j.log-requests}")
        Optional<Boolean> logResponses();

        /**
         * Whether to enable the integration. Defaults to {@code true}, which means requests are made to the watsonx.ai
         * provider. Set to {@code false} to
         * disable all requests.
         */
        @WithDefault("true")
        Boolean enableIntegration();

        /**
         * IAM authentication related settings.
         */
        IAMConfig iam();

        /**
         * Cloud Object Storage related settings.
         * <p>
         * This configuration is only required when using the {@code TextExtraction} class.
         */
        Optional<TextExtractionConfig> textExtraction();

        /**
         * Chat model related settings.
         */
        ChatModelConfig chatModel();

        /**
         * Generation model related settings.
         */
        GenerationModelConfig generationModel();

        /**
         * Embedding model related settings.
         */
        EmbeddingModelConfig embeddingModel();

        /**
         * Scoring model related settings.
         */
        ScoringModelConfig scoringModel();
    }
}
