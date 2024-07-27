package io.quarkiverse.langchain4j.watsonx.deployment;

import static io.quarkiverse.langchain4j.deployment.AiServicesProcessor.IS_METHOD_PARAMETER_ANNOTATION;
import static io.quarkiverse.langchain4j.deployment.LangChain4jDotNames.CHAT_MODEL;
import static io.quarkiverse.langchain4j.deployment.LangChain4jDotNames.EMBEDDING_MODEL;
import static io.quarkiverse.langchain4j.deployment.LangChain4jDotNames.STREAMING_CHAT_MODEL;
import static io.quarkiverse.langchain4j.deployment.LangChain4jDotNames.TOKEN_COUNT_ESTIMATOR;
import static io.quarkiverse.langchain4j.deployment.TemplateUtil.getTemplateFromAnnotationInstance;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.jandex.AnnotationInstance;

import io.quarkiverse.langchain4j.ModelName;
import io.quarkiverse.langchain4j.deployment.LangChain4jDotNames;
import io.quarkiverse.langchain4j.deployment.items.ChatModelProviderCandidateBuildItem;
import io.quarkiverse.langchain4j.deployment.items.EmbeddingModelProviderCandidateBuildItem;
import io.quarkiverse.langchain4j.deployment.items.SelectedChatModelProviderBuildItem;
import io.quarkiverse.langchain4j.deployment.items.SelectedEmbeddingModelCandidateBuildItem;
import io.quarkiverse.langchain4j.runtime.NamedConfigUtil;
import io.quarkiverse.langchain4j.watsonx.deployment.items.PromptFormatterBuildItem;
import io.quarkiverse.langchain4j.watsonx.prompt.PromptFormatter;
import io.quarkiverse.langchain4j.watsonx.prompt.PromptFormatterMapper;
import io.quarkiverse.langchain4j.watsonx.runtime.WatsonxRecorder;
import io.quarkiverse.langchain4j.watsonx.runtime.config.LangChain4jWatsonxConfig;
import io.quarkiverse.langchain4j.watsonx.runtime.config.LangChain4jWatsonxFixedRuntimeConfig;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;

public class WatsonxProcessor {

    private static final String FEATURE = "langchain4j-watsonx";
    private static final String PROVIDER = "watsonx";
    private static final String PROMPT_FORMATTER_NO_DINAMIC = "The \"chat-model.prompt-formatter\" property cannot be set to \"true\" if you are generating the prompt dinamically. Found in class %s";
    private static final String PROMPT_FORMATTER_SPECIAL_TOKEN = "The prompt in the AIService \"%s\" already contains one or more tokens for the model \"%s\". To continue, remove the tokens or set the variable of the property \"chat-model.prompt-formatter\" to \"false\"";
    private static final String PROMPT_FORMATTER_NO_ANNOTATION = "If the \"chat-model.prompt-formatter\" property is set to \"true\", all @RegisterAiService annotated classes assigned to the %s provider with the model name \"%s\" must contain at least one @UserMessage. Found in class %s";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    public void providerCandidates(BuildProducer<ChatModelProviderCandidateBuildItem> chatProducer,
            BuildProducer<EmbeddingModelProviderCandidateBuildItem> embeddingProducer,
            LangChain4jWatsonBuildConfig config) {

        if (config.chatModel().enabled().isEmpty() || config.chatModel().enabled().get()) {
            chatProducer.produce(new ChatModelProviderCandidateBuildItem(PROVIDER));
        }

        if (config.embeddingModel().enabled().isEmpty() || config.embeddingModel().enabled().get()) {
            embeddingProducer.produce(new EmbeddingModelProviderCandidateBuildItem(PROVIDER));
        }
    }

    @BuildStep
    void createPromptFormatters(LangChain4jWatsonxFixedRuntimeConfig fixedRuntimeConfig, CombinedIndexBuildItem indexBuildItem,
            List<SelectedChatModelProviderBuildItem> selectedChatItem, BuildProducer<PromptFormatterBuildItem> producer)
            throws Exception {

        var index = indexBuildItem.getIndex();
        var annotationInstances = index.getAnnotations(LangChain4jDotNames.REGISTER_AI_SERVICES);

        for (var selected : selectedChatItem) {
            if (PROVIDER.equals(selected.getProvider())) {
                String configName = selected.getConfigName();

                String modelId = NamedConfigUtil.isDefault(configName)
                        ? fixedRuntimeConfig.defaultConfig().chatModel().modelId()
                        : fixedRuntimeConfig.namedConfig().get(configName).chatModel().modelId();

                boolean promptFormatterIsEnabled = NamedConfigUtil.isDefault(configName)
                        ? fixedRuntimeConfig.defaultConfig().chatModel().promptFormatter()
                        : fixedRuntimeConfig.namedConfig().get(configName).chatModel().promptFormatter();

                if (!promptFormatterIsEnabled) {
                    producer.produce(new PromptFormatterBuildItem(configName, PromptFormatterMapper.getDefault()));
                    continue;
                }

                PromptFormatter promptFormatter = PromptFormatterMapper.get(modelId);

                var optional = annotationInstances.stream()
                        .filter(annotationInstance -> {
                            var modelName = annotationInstance.value("modelName");
                            if (modelName == null) {
                                return configName.equals(NamedConfigUtil.DEFAULT_NAME);
                            } else {
                                return configName.equals(modelName.asString());
                            }
                        }).findFirst();

                // No RegisterAiService annotated class found, default PromptFormatter is used.
                if (optional.isEmpty()) {
                    producer.produce(new PromptFormatterBuildItem(configName, PromptFormatterMapper.getDefault()));
                    continue;
                }

                var classInfo = optional.get().target().asClass();
                var systemMessage = getTemplateFromAnnotationInstance(classInfo.annotation(LangChain4jDotNames.SYSTEM_MESSAGE));
                var userMessage = getTemplateFromAnnotationInstance(classInfo.annotation(LangChain4jDotNames.USER_MESSAGE));

                // Check if the UserMessage annotation is a method parameter.
                // In this case the prompt formatter can not be used.
                if (userMessage.isEmpty()) {

                    var userMessageMethodParameter = classInfo.annotations(LangChain4jDotNames.USER_MESSAGE)
                            .stream()
                            .filter(IS_METHOD_PARAMETER_ANNOTATION)
                            .findFirst();

                    if (userMessageMethodParameter.isPresent()) {
                        throw new RuntimeException(PROMPT_FORMATTER_NO_DINAMIC.formatted(classInfo.name().toString()));
                    }
                }

                if (userMessage.isEmpty() && systemMessage.isEmpty()) {
                    throw new RuntimeException(
                            PROMPT_FORMATTER_NO_ANNOTATION.formatted(PROVIDER, configName, classInfo.name().toString()));
                }

                // Check if there is at least one token in the SystemMessage or UserMessage template.
                var tokenAlreadyExist = promptFormatter.tokens().stream()
                        .filter(token -> systemMessage.contains(token) || userMessage.contains(token))
                        .findFirst();

                if (tokenAlreadyExist.isPresent()) {
                    throw new RuntimeException(PROMPT_FORMATTER_SPECIAL_TOKEN.formatted(classInfo.name().toString(), modelId));
                }

                producer.produce(new PromptFormatterBuildItem(configName, promptFormatter));
            }
        }
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void generateBeans(WatsonxRecorder recorder, LangChain4jWatsonxConfig runtimeConfig,
            LangChain4jWatsonxFixedRuntimeConfig fixedRuntimeConfig,
            List<SelectedChatModelProviderBuildItem> selectedChatItem,
            List<SelectedEmbeddingModelCandidateBuildItem> selectedEmbedding,
            List<PromptFormatterBuildItem> promptFormatters,
            BuildProducer<SyntheticBeanBuildItem> beanProducer) {

        for (var selected : selectedChatItem) {
            if (PROVIDER.equals(selected.getProvider())) {
                String configName = selected.getConfigName();

                var promptFormatter = promptFormatters.stream()
                        .filter(obj -> obj.getConfigName().equals(configName))
                        .map(PromptFormatterBuildItem::getPromptFormatter)
                        .findFirst().orElseThrow(() -> new RuntimeException(
                                "No PromptFormatter found for the configuration %s".formatted(configName)));

                var chatModel = recorder.chatModel(runtimeConfig, fixedRuntimeConfig, configName, promptFormatter);
                var chatBuilder = SyntheticBeanBuildItem
                        .configure(CHAT_MODEL)
                        .setRuntimeInit()
                        .defaultBean()
                        .scope(ApplicationScoped.class)
                        .supplier(chatModel);
                addQualifierIfNecessary(chatBuilder, configName);
                beanProducer.produce(chatBuilder.done());

                var tokenizerBuilder = SyntheticBeanBuildItem
                        .configure(TOKEN_COUNT_ESTIMATOR)
                        .setRuntimeInit()
                        .defaultBean()
                        .scope(ApplicationScoped.class)
                        .supplier(chatModel);
                addQualifierIfNecessary(tokenizerBuilder, configName);
                beanProducer.produce(tokenizerBuilder.done());

                var streamingBuilder = SyntheticBeanBuildItem
                        .configure(STREAMING_CHAT_MODEL)
                        .setRuntimeInit()
                        .defaultBean()
                        .scope(ApplicationScoped.class)
                        .supplier(recorder.streamingChatModel(runtimeConfig, fixedRuntimeConfig, configName, promptFormatter));
                addQualifierIfNecessary(streamingBuilder, configName);
                beanProducer.produce(streamingBuilder.done());
            }
        }

        for (var selected : selectedEmbedding) {
            if (PROVIDER.equals(selected.getProvider())) {
                String configName = selected.getConfigName();
                var builder = SyntheticBeanBuildItem
                        .configure(EMBEDDING_MODEL)
                        .setRuntimeInit()
                        .defaultBean()
                        .unremovable()
                        .scope(ApplicationScoped.class)
                        .supplier(recorder.embeddingModel(runtimeConfig, configName));
                addQualifierIfNecessary(builder, configName);
                beanProducer.produce(builder.done());
            }
        }

    }

    private void addQualifierIfNecessary(SyntheticBeanBuildItem.ExtendedBeanConfigurator builder, String configName) {
        if (!NamedConfigUtil.isDefault(configName)) {
            builder.addQualifier(AnnotationInstance.builder(ModelName.class).add("value", configName).build());
        }
    }
}
