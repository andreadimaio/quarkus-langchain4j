package io.quarkiverse.langchain4j.runtime.config;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigGroup;

@ConfigGroup
public interface ModerationModelBuild {

    /**
     * The model provider to use
     */
    Optional<ModelProvider> provider();
}