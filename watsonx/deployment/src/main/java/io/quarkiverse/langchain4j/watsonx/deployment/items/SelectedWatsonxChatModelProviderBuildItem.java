package io.quarkiverse.langchain4j.watsonx.deployment.items;

import io.quarkus.builder.item.MultiBuildItem;

public final class SelectedWatsonxChatModelProviderBuildItem extends MultiBuildItem {

    private final String provider;
    private final String modelName;
    private final String deployment;

    public SelectedWatsonxChatModelProviderBuildItem(String provider, String modelName, String deployment) {
        this.provider = provider;
        this.modelName = modelName;
        this.deployment = deployment;
    }

    public String getProvider() {
        return provider;
    }

    public String getModelName() {
        return modelName;
    }

    public String getDeployment() {
        return deployment;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((provider == null) ? 0 : provider.hashCode());
        result = prime * result + ((modelName == null) ? 0 : modelName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SelectedWatsonxChatModelProviderBuildItem other = (SelectedWatsonxChatModelProviderBuildItem) obj;
        if (provider == null) {
            if (other.provider != null)
                return false;
        } else if (!provider.equals(other.provider))
            return false;
        if (modelName == null) {
            if (other.modelName != null)
                return false;
        } else if (!modelName.equals(other.modelName))
            return false;
        return true;
    }
}
