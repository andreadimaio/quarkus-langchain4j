package io.quarkiverse.langchain4j.watsonx.deployment.items;

import org.jboss.jandex.DotName;

import io.quarkus.builder.item.MultiBuildItem;

public final class BuiltinToolClassBuildItem extends MultiBuildItem {

    private DotName dotName;
    private String wxBaseUrl;

    public BuiltinToolClassBuildItem(DotName dotName, String wxBaseUrl) {
        this.dotName = dotName;
        this.wxBaseUrl = wxBaseUrl;
    }

    public DotName getDotName() {
        return dotName;
    }

    public String getWxBaseUrl() {
        return wxBaseUrl;
    }
}
