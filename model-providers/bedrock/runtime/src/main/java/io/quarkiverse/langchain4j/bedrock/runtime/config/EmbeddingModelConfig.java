package io.quarkiverse.langchain4j.bedrock.runtime.config;

import java.util.Optional;
import java.util.OptionalInt;

import io.quarkus.runtime.annotations.ConfigDocDefault;
import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.WithDefault;

@ConfigGroup
public interface EmbeddingModelConfig extends AwsClientConfig {

    /**
     * Model name to use
     */
    @WithDefault("cohere.embed-english-v3")
    String modelId();

    interface BedrockTitanConfig {

        /**
         * The number of dimensions the output embedding should have
         */
        OptionalInt dimensions();

        /**
         * Flag indicating whether to normalize the output embedding
         */
        Optional<Boolean> normalize();
    }

    interface BedrockCohereConfig {

        /**
         * Prepends special tokens to differentiate each type from one another.
         * You should not mix different types together, except when mixing types for search and retrieval.
         * In this case, embed your corpus with the search_document type and embedded queries with type search_query type.
         */
        Optional<String> inputType();

        /**
         * Specifies how the API handles inputs longer than the maximum token length
         */
        Optional<String> truncate();
    }

    /**
     * Titan specific configuration
     */
    BedrockTitanConfig titan();

    /**
     * Cohere specific configuration
     */
    BedrockCohereConfig cohere();

    /**
     * Whether embedding model requests should be logged
     */
    @ConfigDocDefault("false")
    Optional<Boolean> logRequests();

    /**
     * Whether embedding model responses should be logged
     */
    @ConfigDocDefault("false")
    Optional<Boolean> logResponses();

    /**
     * Whether embedding model body in request and response should be logged
     */
    @ConfigDocDefault("false")
    Optional<Boolean> logBody();

    /**
     * Aws sdk related configurations for embedding models
     */
    AwsConfig aws();

    /**
     * Http client related configurations for chat models
     */
    HttpClientConfig client();

}
