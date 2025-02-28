package io.quarkiverse.langchain4j.watsonx.deployment.items;

import io.quarkus.builder.item.MultiBuildItem;

public final class AgentLabBuildItem extends MultiBuildItem {

    private String configName;
    private String deploymentId;

    public AgentLabBuildItem(String configName, String deploymentId) {
        this.configName = configName;
        this.deploymentId = deploymentId;
    }

    public String getConfigName() {
        return configName;
    }

    public String getDeploymentId() {
        return deploymentId;
    }
}
