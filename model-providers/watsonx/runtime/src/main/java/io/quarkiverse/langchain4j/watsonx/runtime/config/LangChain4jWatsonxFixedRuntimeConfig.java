package io.quarkiverse.langchain4j.watsonx.runtime.config;

import static io.quarkus.runtime.annotations.ConfigPhase.BUILD_AND_RUN_TIME_FIXED;

import java.util.Map;
import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigDocMapKey;
import io.quarkus.runtime.annotations.ConfigDocSection;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithDefaults;
import io.smallrye.config.WithParentName;

@ConfigRoot(phase = BUILD_AND_RUN_TIME_FIXED)
@ConfigMapping(prefix = "quarkus.langchain4j.watsonx")
public interface LangChain4jWatsonxFixedRuntimeConfig {

    /**
     * Default model config.
     */
    @WithParentName
    WatsonxConfig defaultConfig();

    /**
     * Named model config.
     */
    @ConfigDocSection
    @ConfigDocMapKey("model-name")
    @WithParentName
    @WithDefaults
    Map<String, WatsonxConfig> namedConfig();

    interface WatsonxConfig {

        /**
         * Base URL of the watsonx.ai API.
         */
        Optional<String> baseUrl();

        /**
         * Base URL for prompts and agent tools in the watsonx.ai API.
         * <p>
         * If empty, this URL will be automatically calculated based on the {@code base-url}.
         */
        Optional<String> wxBaseUrl();

        /**
         * IBM Cloud API key.
         */
        Optional<String> apiKey();

        /**
         * IAM authentication related settings.
         */
        IAMConfig iam();

        /**
         * Specifies the mode of interaction with the LLM model.
         * <p>
         * This property allows you to choose between two modes of operation:
         * <ul>
         * <li><strong>chat</strong>: prompts are automatically enriched with the specific tags defined by the model</li>
         * <li><strong>generation</strong>: prompts require manual specification of tags</li>
         * </ul>
         * <p>
         * <strong>Allowable values:</strong> <code>[chat, generation]</code>
         */
        @WithDefault("chat")
        String mode();
    }
}
