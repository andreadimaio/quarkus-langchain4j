package io.quarkiverse.langchain4j.watsonx.deployment.items;

import org.jboss.jandex.DotName;

import io.quarkus.builder.item.MultiBuildItem;

public final class BuiltinToolClassBuildItem extends MultiBuildItem {

    private DotName dotName;

    public BuiltinToolClassBuildItem(DotName dotName) {
        this.dotName = dotName;
    }

    public DotName getDotName() {
        return dotName;
    }
}
