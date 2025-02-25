package io.quarkiverse.langchain4j.watsonx.deployment;

import static io.quarkiverse.langchain4j.deployment.LangChain4jDotNames.CHAT_MODEL;
import static io.quarkiverse.langchain4j.deployment.LangChain4jDotNames.EMBEDDING_MODEL;
import static io.quarkiverse.langchain4j.deployment.LangChain4jDotNames.SCORING_MODEL;
import static io.quarkiverse.langchain4j.deployment.LangChain4jDotNames.STREAMING_CHAT_MODEL;
import static io.quarkiverse.langchain4j.deployment.LangChain4jDotNames.TOKEN_COUNT_ESTIMATOR;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget.Kind;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.Type;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import io.quarkiverse.langchain4j.ModelName;
import io.quarkiverse.langchain4j.ToolBox;
import io.quarkiverse.langchain4j.deployment.DotNames;
import io.quarkiverse.langchain4j.deployment.LangChain4jDotNames;
import io.quarkiverse.langchain4j.deployment.items.ChatModelProviderCandidateBuildItem;
import io.quarkiverse.langchain4j.deployment.items.EmbeddingModelProviderCandidateBuildItem;
import io.quarkiverse.langchain4j.deployment.items.ScoringModelProviderCandidateBuildItem;
import io.quarkiverse.langchain4j.deployment.items.SelectedChatModelProviderBuildItem;
import io.quarkiverse.langchain4j.deployment.items.SelectedEmbeddingModelCandidateBuildItem;
import io.quarkiverse.langchain4j.deployment.items.SelectedScoringModelProviderBuildItem;
import io.quarkiverse.langchain4j.runtime.NamedConfigUtil;
import io.quarkiverse.langchain4j.watsonx.deployment.items.BuiltinToolClassBuildItem;
import io.quarkiverse.langchain4j.watsonx.runtime.BuiltinToolRecorder;
import io.quarkiverse.langchain4j.watsonx.runtime.WatsonxRecorder;
import io.quarkiverse.langchain4j.watsonx.runtime.config.LangChain4jWatsonxConfig;
import io.quarkiverse.langchain4j.watsonx.runtime.config.LangChain4jWatsonxFixedRuntimeConfig;
import io.quarkus.arc.SyntheticCreationalContext;
import io.quarkus.arc.deployment.BeanDiscoveryFinishedBuildItem;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.Capability;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.resteasy.reactive.spi.MessageBodyReaderOverrideBuildItem;
import io.quarkus.resteasy.reactive.spi.MessageBodyWriterOverrideBuildItem;
import io.smallrye.config.Priorities;

public class WatsonxProcessor {

    private static final String FEATURE = "langchain4j-watsonx";
    private static final String PROVIDER = "watsonx";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    public void providerCandidates(BuildProducer<ChatModelProviderCandidateBuildItem> chatProducer,
            BuildProducer<EmbeddingModelProviderCandidateBuildItem> embeddingProducer,
            BuildProducer<ScoringModelProviderCandidateBuildItem> scoringProducer,
            LangChain4jWatsonBuildConfig config) {

        if (config.chatModel().enabled().isEmpty() || config.chatModel().enabled().get()) {
            chatProducer.produce(new ChatModelProviderCandidateBuildItem(PROVIDER));
        }

        if (config.embeddingModel().enabled().isEmpty() || config.embeddingModel().enabled().get()) {
            embeddingProducer.produce(new EmbeddingModelProviderCandidateBuildItem(PROVIDER));
        }

        if (config.scoringModel().enabled().isEmpty() || config.scoringModel().enabled().get()) {
            scoringProducer.produce(new ScoringModelProviderCandidateBuildItem(PROVIDER));
        }
    }

    @BuildStep
    void discoverBuiltinToolBeans(
            CombinedIndexBuildItem indexBuildItem,
            BeanDiscoveryFinishedBuildItem beans,
            LangChain4jWatsonxFixedRuntimeConfig fixedRuntimeConfig,
            BuildProducer<BuiltinToolClassBuildItem> producer) {

        String errorMessage = "To use the BuiltinToolClasses, you must set the property 'quarkus.langchain4j.watsonx.%s'";

        Set<DotName> dotNames = new HashSet<>();
        beans.getInjectionPoints().stream()
                .map(ip -> ip.getRequiredType().name())
                .filter(this::isABuiltinToolClass)
                .forEach(dotNames::add);

        IndexView indexView = indexBuildItem.getIndex();
        indexView.getAnnotations(LangChain4jDotNames.REGISTER_AI_SERVICES).stream()
                .filter(instance -> instance.target().kind().equals(Kind.CLASS))
                .filter(instance -> instance.value("tools") != null)
                .flatMap(instance -> Stream.of(instance.value("tools").asClassArray()))
                .map(Type::name)
                .filter(this::isABuiltinToolClass)
                .forEach(dotNames::add);

        indexView.getAnnotations(ToolBox.class).stream()
                .filter(instance -> instance.value() != null)
                .flatMap(instance -> Stream.of(instance.value().asClassArray()))
                .map(Type::name)
                .filter(this::isABuiltinToolClass)
                .forEach(dotNames::add);

        if (dotNames.isEmpty())
            return; // Nothing to produce..

        String wxBaseUrl = fixedRuntimeConfig.defaultConfig().wxBaseUrl().orElseGet(() -> {
            var url = fixedRuntimeConfig.defaultConfig().baseUrl()
                    .orElseThrow(() -> new RuntimeException(errorMessage.formatted("base-url")));
            return switch (url) {
                case "https://us-south.ml.cloud.ibm.com" -> "https://api.dataplatform.cloud.ibm.com/wx";
                case "https://eu-de.ml.cloud.ibm.com" -> "https://api.eu-de.dataplatform.cloud.ibm.com/wx";
                case "https://eu-gb.ml.cloud.ibm.com" -> "https://api.eu-gb.dataplatform.cloud.ibm.com/wx";
                case "https://jp-tok.ml.cloud.ibm.com" -> "https://api.jp-tok.dataplatform.cloud.ibm.com/wx";
                case "https://au-syd.ml.cloud.ibm.com" -> "https://api.au-syd.dai.cloud.ibm.com/wx";
                case "https://ca-tor.ml.cloud.ibm.com" -> "https://api.ca-tor.dai.cloud.ibm.com/wx";
                default -> throw new RuntimeException(
                        "The property 'quarkus.langchain4j.watsonx.base-url' does not have a correct url. Use one of the urls given in the documentation or use the property 'quarkus.langchain4j.watsonx.wx-base-url' to set a custom url for the agent-tool service");
            };
        });

        fixedRuntimeConfig.defaultConfig().apiKey()
                .orElseThrow(() -> new RuntimeException(errorMessage.formatted("api-key")));
        dotNames.stream()
                .forEach(dotName -> producer.produce(new BuiltinToolClassBuildItem(dotName, wxBaseUrl)));
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void generateBuiltinToolBeans(
            BuiltinToolRecorder recorder,
            LangChain4jWatsonxConfig runtimeConfig,
            LangChain4jWatsonxFixedRuntimeConfig fixedRuntimeConfig,
            List<BuiltinToolClassBuildItem> builtinToolClasses,
            BuildProducer<SyntheticBeanBuildItem> beanProducer) {

        for (BuiltinToolClassBuildItem builtinToolClass : builtinToolClasses) {
            var baseUrl = builtinToolClass.getWxBaseUrl();
            var builder = SyntheticBeanBuildItem
                    .configure(builtinToolClass.getDotName())
                    .setRuntimeInit()
                    .defaultBean()
                    .scope(ApplicationScoped.class);

            if (builtinToolClass.getDotName().equals(WatsonxDotNames.GOOGLE_SEARCH_TOOL))
                builder.supplier(recorder.googleSearchTool(baseUrl, runtimeConfig, fixedRuntimeConfig));
            else if (builtinToolClass.getDotName().equals(WatsonxDotNames.WEB_CRAWLER_TOOL))
                builder.supplier(recorder.webCrawlerTool(baseUrl, runtimeConfig, fixedRuntimeConfig));
            else if (builtinToolClass.getDotName().equals(WatsonxDotNames.WEATHER_TOOL))
                builder.supplier(recorder.weatherTool(baseUrl, runtimeConfig, fixedRuntimeConfig));
            else
                throw new RuntimeException("BuiltinToolClass not recognised");

            beanProducer.produce(builder.done());
        }
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void generateLangchain4jBeans(
            WatsonxRecorder recorder,
            LangChain4jWatsonxConfig runtimeConfig,
            LangChain4jWatsonxFixedRuntimeConfig fixedRuntimeConfig,
            List<SelectedChatModelProviderBuildItem> selectedChatItem,
            List<SelectedEmbeddingModelCandidateBuildItem> selectedEmbedding,
            List<SelectedScoringModelProviderBuildItem> selectedScoring,
            BuildProducer<SyntheticBeanBuildItem> beanProducer) {

        for (var selected : selectedChatItem) {

            if (!PROVIDER.equals(selected.getProvider()))
                continue;

            String configName = selected.getConfigName();
            String mode = NamedConfigUtil.isDefault(configName)
                    ? fixedRuntimeConfig.defaultConfig().mode()
                    : fixedRuntimeConfig.namedConfig().get(configName).mode();

            Function<SyntheticCreationalContext<ChatLanguageModel>, ChatLanguageModel> chatLanguageModel;
            Function<SyntheticCreationalContext<StreamingChatLanguageModel>, StreamingChatLanguageModel> streamingChatLanguageModel;

            if (mode.equalsIgnoreCase("chat")) {
                chatLanguageModel = recorder.chatModel(fixedRuntimeConfig, runtimeConfig, configName);
                streamingChatLanguageModel = recorder.streamingChatModel(fixedRuntimeConfig, runtimeConfig, configName);
            } else if (mode.equalsIgnoreCase("generation")) {
                chatLanguageModel = recorder.generationModel(fixedRuntimeConfig, runtimeConfig, configName);
                streamingChatLanguageModel = recorder.generationStreamingModel(fixedRuntimeConfig, runtimeConfig, configName);
            } else {
                throw new RuntimeException(
                        "The \"mode\" value for the model \"%s\" is not valid. Choose one between [\"chat\", \"generation\"]"
                                .formatted(mode, configName));
            }

            var chatBuilder = SyntheticBeanBuildItem
                    .configure(CHAT_MODEL)
                    .setRuntimeInit()
                    .defaultBean()
                    .scope(ApplicationScoped.class)
                    .addInjectionPoint(ParameterizedType.create(DotNames.CDI_INSTANCE,
                            new Type[] { ClassType.create(DotNames.CHAT_MODEL_LISTENER) }, null))
                    .createWith(chatLanguageModel);

            addQualifierIfNecessary(chatBuilder, configName);
            beanProducer.produce(chatBuilder.done());

            var tokenizerBuilder = SyntheticBeanBuildItem
                    .configure(TOKEN_COUNT_ESTIMATOR)
                    .setRuntimeInit()
                    .defaultBean()
                    .scope(ApplicationScoped.class)
                    .addInjectionPoint(ParameterizedType.create(DotNames.CDI_INSTANCE,
                            new Type[] { ClassType.create(DotNames.CHAT_MODEL_LISTENER) }, null))
                    .createWith(chatLanguageModel);

            addQualifierIfNecessary(tokenizerBuilder, configName);
            beanProducer.produce(tokenizerBuilder.done());

            var streamingBuilder = SyntheticBeanBuildItem
                    .configure(STREAMING_CHAT_MODEL)
                    .setRuntimeInit()
                    .defaultBean()
                    .scope(ApplicationScoped.class)
                    .addInjectionPoint(ParameterizedType.create(DotNames.CDI_INSTANCE,
                            new Type[] { ClassType.create(DotNames.CHAT_MODEL_LISTENER) }, null))
                    .createWith(streamingChatLanguageModel);

            addQualifierIfNecessary(streamingBuilder, configName);
            beanProducer.produce(streamingBuilder.done());
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
                        .supplier(recorder.embeddingModel(fixedRuntimeConfig, runtimeConfig, configName));
                addQualifierIfNecessary(builder, configName);
                beanProducer.produce(builder.done());
            }
        }

        for (var selected : selectedScoring) {
            if (PROVIDER.equals(selected.getProvider())) {
                String configName = selected.getConfigName();
                var builder = SyntheticBeanBuildItem
                        .configure(SCORING_MODEL)
                        .setRuntimeInit()
                        .defaultBean()
                        .unremovable()
                        .scope(ApplicationScoped.class)
                        .supplier(recorder.scoringModel(fixedRuntimeConfig, runtimeConfig, configName));
                addQualifierIfNecessary(builder, configName);
                beanProducer.produce(builder.done());
            }
        }
    }

    private boolean isABuiltinToolClass(DotName dotName) {
        if (dotName.equals(WatsonxDotNames.WEB_CRAWLER_TOOL))
            return true;
        else if (dotName.equals(WatsonxDotNames.GOOGLE_SEARCH_TOOL))
            return true;
        else if (dotName.equals(WatsonxDotNames.WEATHER_TOOL))
            return true;
        else
            return false;
    }

    private void addQualifierIfNecessary(SyntheticBeanBuildItem.ExtendedBeanConfigurator builder, String configName) {
        if (!NamedConfigUtil.isDefault(configName)) {
            builder.addQualifier(AnnotationInstance.builder(ModelName.class).add("value", configName).build());
        }
    }

    /**
     * When both {@code rest-client-jackson} and {@code rest-client-jsonb} are present on the classpath we need to make sure
     * that Jackson is used. This is not a proper solution as it affects all clients, but it's better than the having the
     * reader/writers be selected at random.
     */
    @BuildStep
    public void deprioritizeJsonb(Capabilities capabilities,
            BuildProducer<MessageBodyReaderOverrideBuildItem> readerOverrideProducer,
            BuildProducer<MessageBodyWriterOverrideBuildItem> writerOverrideProducer) {
        if (capabilities.isPresent(Capability.REST_CLIENT_REACTIVE_JSONB)) {
            readerOverrideProducer.produce(
                    new MessageBodyReaderOverrideBuildItem("org.jboss.resteasy.reactive.server.jsonb.JsonbMessageBodyReader",
                            Priorities.APPLICATION + 1, true));
            writerOverrideProducer.produce(new MessageBodyWriterOverrideBuildItem(
                    "org.jboss.resteasy.reactive.server.jsonb.JsonbMessageBodyWriter", Priorities.APPLICATION + 1, true));
        }
    }
}
