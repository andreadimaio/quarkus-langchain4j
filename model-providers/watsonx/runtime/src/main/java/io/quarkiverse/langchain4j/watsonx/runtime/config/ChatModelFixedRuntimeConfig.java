package io.quarkiverse.langchain4j.watsonx.runtime.config;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.WithDefault;

@ConfigGroup
public interface ChatModelFixedRuntimeConfig {

    /**
     * Model id to use.
     * <p>
     * To view the complete model list, <a href=
     * "https://dataplatform.cloud.ibm.com/docs/content/wsj/analyze-data/fm-api-model-ids.html?context=wx&audience=wdp#model-ids">click
     * here</a>.
     */
    @WithDefault("ibm/granite-13b-chat-v2")
    String modelId();

    /**
     * Indicates whether prompt formatter is enabled or disabled. When enabled, the prompt is automatically enriched, and tags
     * are
     * added to the prompt based on the model used. This property can be set to:
     * <p>
     * - <code>true</code>: Tags are added automatically. If the model used doesn't have a PromptFormatter, the default version
     * is
     * used. If a prompt in the AIService class already contains the special tags, an exception is thrown.
     * <p>
     * - <code>false</code>: No tags are added.
     */
    @WithDefault("false")
    boolean promptFormatter();
}
