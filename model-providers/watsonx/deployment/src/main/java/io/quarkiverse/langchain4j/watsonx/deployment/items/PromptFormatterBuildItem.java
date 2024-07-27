package io.quarkiverse.langchain4j.watsonx.deployment.items;

import io.quarkiverse.langchain4j.watsonx.prompt.PromptFormatter;
import io.quarkus.builder.item.MultiBuildItem;

public final class PromptFormatterBuildItem extends MultiBuildItem {

    private final String configName;
    private final PromptFormatter promptFormatter;

    public PromptFormatterBuildItem(String configName, PromptFormatter promptFormatter) {
        this.configName = configName;
        this.promptFormatter = promptFormatter;
    }

    public String getConfigName() {
        return configName;
    }

    public PromptFormatter getPromptFormatter() {
        return promptFormatter;
    }
}
