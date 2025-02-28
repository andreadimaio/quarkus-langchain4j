package io.quarkiverse.langchain4j.watsonx.runtime;

import static dev.langchain4j.model.chat.request.ToolChoice.REQUIRED;
import static io.quarkiverse.langchain4j.runtime.OptionalUtil.firstOrDefault;
import static io.quarkiverse.langchain4j.watsonx.runtime.TokenGenerationCache.getOrCreateTokenGenerator;

import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.util.TypeLiteral;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.DisabledChatLanguageModel;
import dev.langchain4j.model.chat.DisabledStreamingChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ToolChoice;
import dev.langchain4j.model.embedding.DisabledEmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.scoring.ScoringModel;
import io.quarkiverse.langchain4j.runtime.NamedConfigUtil;
import io.quarkiverse.langchain4j.watsonx.WatsonxChatModel;
import io.quarkiverse.langchain4j.watsonx.WatsonxEmbeddingModel;
import io.quarkiverse.langchain4j.watsonx.WatsonxGenerationModel;
import io.quarkiverse.langchain4j.watsonx.WatsonxScoringModel;
import io.quarkiverse.langchain4j.watsonx.runtime.config.ChatModelConfig;
import io.quarkiverse.langchain4j.watsonx.runtime.config.EmbeddingModelConfig;
import io.quarkiverse.langchain4j.watsonx.runtime.config.GenerationModelConfig;
import io.quarkiverse.langchain4j.watsonx.runtime.config.LangChain4jWatsonxConfig;
import io.quarkiverse.langchain4j.watsonx.runtime.config.LangChain4jWatsonxFixedRuntimeConfig;
import io.quarkiverse.langchain4j.watsonx.runtime.config.ScoringModelConfig;
import io.quarkus.arc.SyntheticCreationalContext;
import io.quarkus.runtime.annotations.Recorder;
import io.smallrye.config.ConfigValidationException;

@Recorder
public class WatsonxRecorder {

    private static final ConfigValidationException.Problem[] EMPTY_PROBLEMS = new ConfigValidationException.Problem[0];
    private static final TypeLiteral<Instance<ChatModelListener>> CHAT_MODEL_LISTENER_TYPE_LITERAL = new TypeLiteral<>() {
    };

    public Function<SyntheticCreationalContext<ChatLanguageModel>, ChatLanguageModel> chatModel(
            LangChain4jWatsonxFixedRuntimeConfig fixedRuntimeConfig,
            LangChain4jWatsonxConfig runtimeConfig,
            String configName, String deploymentId) {

        var watsonxRuntimeConfig = correspondingWatsonxRuntimeConfig(runtimeConfig, configName);
        var watsonxFixedRuntimeConfig = correspondingWatsonxFixedRuntimeConfig(fixedRuntimeConfig, configName);

        if (watsonxRuntimeConfig.enableIntegration()) {
            var builder = chatBuilder(fixedRuntimeConfig, runtimeConfig, configName, deploymentId);
            var apiKey = firstOrDefault(
                    null,
                    watsonxFixedRuntimeConfig.apiKey(),
                    fixedRuntimeConfig.defaultConfig().apiKey());
            var iamBaseUrl = watsonxFixedRuntimeConfig.iam().baseUrl();
            var granType = watsonxFixedRuntimeConfig.iam().grantType();
            var duration = watsonxFixedRuntimeConfig.iam().timeout().orElse(Duration.ofSeconds(10));
            return new Function<>() {
                @Override
                public ChatLanguageModel apply(SyntheticCreationalContext<ChatLanguageModel> context) {
                    return builder
                            .tokenGenerator(getOrCreateTokenGenerator(apiKey, iamBaseUrl, granType, duration))
                            .listeners(context.getInjectedReference(CHAT_MODEL_LISTENER_TYPE_LITERAL).stream().toList())
                            .build();
                }
            };

        } else {
            return new Function<>() {
                @Override
                public ChatLanguageModel apply(SyntheticCreationalContext<ChatLanguageModel> context) {
                    return new DisabledChatLanguageModel();
                }
            };
        }
    }

    public Function<SyntheticCreationalContext<StreamingChatLanguageModel>, StreamingChatLanguageModel> streamingChatModel(
            LangChain4jWatsonxFixedRuntimeConfig fixedRuntimeConfig,
            LangChain4jWatsonxConfig runtimeConfig,
            String configName, String deploymentId) {

        var watsonxRuntimeConfig = correspondingWatsonxRuntimeConfig(runtimeConfig, configName);
        var watsonxFixedRuntimeConfig = correspondingWatsonxFixedRuntimeConfig(fixedRuntimeConfig, configName);

        if (watsonxRuntimeConfig.enableIntegration()) {
            var builder = chatBuilder(fixedRuntimeConfig, runtimeConfig, configName, deploymentId);
            var apiKey = firstOrDefault(
                    null,
                    watsonxFixedRuntimeConfig.apiKey(),
                    fixedRuntimeConfig.defaultConfig().apiKey());
            var iamBaseUrl = watsonxFixedRuntimeConfig.iam().baseUrl();
            var granType = watsonxFixedRuntimeConfig.iam().grantType();
            var duration = watsonxFixedRuntimeConfig.iam().timeout().orElse(Duration.ofSeconds(10));
            return new Function<>() {
                @Override
                public StreamingChatLanguageModel apply(SyntheticCreationalContext<StreamingChatLanguageModel> context) {
                    return builder
                            .tokenGenerator(getOrCreateTokenGenerator(apiKey, iamBaseUrl, granType, duration))
                            .listeners(context.getInjectedReference(CHAT_MODEL_LISTENER_TYPE_LITERAL).stream().toList())
                            .build();
                }
            };

        } else {
            return new Function<>() {
                @Override
                public StreamingChatLanguageModel apply(SyntheticCreationalContext<StreamingChatLanguageModel> context) {
                    return new DisabledStreamingChatLanguageModel();
                }
            };
        }
    }

    public Function<SyntheticCreationalContext<ChatLanguageModel>, ChatLanguageModel> generationModel(
            LangChain4jWatsonxFixedRuntimeConfig fixedRuntimeConfig,
            LangChain4jWatsonxConfig runtimeConfig,
            String configName) {

        var watsonxRuntimeConfig = correspondingWatsonxRuntimeConfig(runtimeConfig, configName);
        var watsonxFixedRuntimeConfig = correspondingWatsonxFixedRuntimeConfig(fixedRuntimeConfig, configName);

        if (watsonxRuntimeConfig.enableIntegration()) {
            var builder = generationBuilder(fixedRuntimeConfig, runtimeConfig, configName);
            var apiKey = firstOrDefault(
                    null,
                    watsonxFixedRuntimeConfig.apiKey(),
                    fixedRuntimeConfig.defaultConfig().apiKey());
            var iamBaseUrl = watsonxFixedRuntimeConfig.iam().baseUrl();
            var granType = watsonxFixedRuntimeConfig.iam().grantType();
            var duration = watsonxFixedRuntimeConfig.iam().timeout().orElse(Duration.ofSeconds(10));
            return new Function<>() {
                @Override
                public ChatLanguageModel apply(SyntheticCreationalContext<ChatLanguageModel> context) {
                    return builder
                            .tokenGenerator(getOrCreateTokenGenerator(apiKey, iamBaseUrl, granType, duration))
                            .listeners(context.getInjectedReference(CHAT_MODEL_LISTENER_TYPE_LITERAL).stream().toList())
                            .build();
                }
            };

        } else {
            return new Function<>() {
                @Override
                public ChatLanguageModel apply(SyntheticCreationalContext<ChatLanguageModel> context) {
                    return new DisabledChatLanguageModel();
                }

            };
        }
    }

    public Function<SyntheticCreationalContext<StreamingChatLanguageModel>, StreamingChatLanguageModel> generationStreamingModel(
            LangChain4jWatsonxFixedRuntimeConfig fixedRuntimeConfig,
            LangChain4jWatsonxConfig runtimeConfig,
            String configName) {

        var watsonxRuntimeConfig = correspondingWatsonxRuntimeConfig(runtimeConfig, configName);
        var watsonxFixedRuntimeConfig = correspondingWatsonxFixedRuntimeConfig(fixedRuntimeConfig, configName);

        if (watsonxRuntimeConfig.enableIntegration()) {
            var builder = generationBuilder(fixedRuntimeConfig, runtimeConfig, configName);
            var apiKey = firstOrDefault(
                    null,
                    watsonxFixedRuntimeConfig.apiKey(),
                    fixedRuntimeConfig.defaultConfig().apiKey());
            var iamBaseUrl = watsonxFixedRuntimeConfig.iam().baseUrl();
            var granType = watsonxFixedRuntimeConfig.iam().grantType();
            var duration = watsonxFixedRuntimeConfig.iam().timeout().orElse(Duration.ofSeconds(10));
            return new Function<>() {
                @Override
                public StreamingChatLanguageModel apply(SyntheticCreationalContext<StreamingChatLanguageModel> context) {
                    return builder
                            .tokenGenerator(getOrCreateTokenGenerator(apiKey, iamBaseUrl, granType, duration))
                            .listeners(context.getInjectedReference(CHAT_MODEL_LISTENER_TYPE_LITERAL).stream().toList())
                            .build();
                }
            };

        } else {
            return new Function<>() {
                @Override
                public StreamingChatLanguageModel apply(SyntheticCreationalContext<StreamingChatLanguageModel> context) {
                    return new DisabledStreamingChatLanguageModel();
                }

            };
        }
    }

    public Supplier<EmbeddingModel> embeddingModel(
            LangChain4jWatsonxFixedRuntimeConfig fixedRuntimeConfig,
            LangChain4jWatsonxConfig runtimeConfig,
            String configName) {

        var watsonxFixedRuntimeConfig = correspondingWatsonxFixedRuntimeConfig(fixedRuntimeConfig, configName);
        var watsonxConfig = correspondingWatsonxRuntimeConfig(runtimeConfig, configName);

        if (watsonxConfig.enableIntegration()) {
            var configProblems = checkConfigurations(fixedRuntimeConfig, runtimeConfig, configName);

            if (!configProblems.isEmpty()) {
                throw new ConfigValidationException(configProblems.toArray(EMPTY_PROBLEMS));
            }

            var apiKey = firstOrDefault(
                    null,
                    watsonxFixedRuntimeConfig.apiKey(),
                    fixedRuntimeConfig.defaultConfig().apiKey());
            var iamBaseUrl = watsonxFixedRuntimeConfig.iam().baseUrl();
            var granType = watsonxFixedRuntimeConfig.iam().grantType();
            var duration = watsonxFixedRuntimeConfig.iam().timeout().orElse(Duration.ofSeconds(10));

            URL url;
            try {
                url = URI.create(
                        firstOrDefault(
                                null,
                                watsonxFixedRuntimeConfig.baseUrl(),
                                fixedRuntimeConfig.defaultConfig().baseUrl()))
                        .toURL();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            EmbeddingModelConfig embeddingModelConfig = watsonxConfig.embeddingModel();
            var builder = WatsonxEmbeddingModel.builder()
                    .url(url)
                    .timeout(watsonxConfig.timeout().orElse(Duration.ofSeconds(10)))
                    .logRequests(firstOrDefault(false, embeddingModelConfig.logRequests(), watsonxConfig.logRequests()))
                    .logResponses(firstOrDefault(false, embeddingModelConfig.logResponses(), watsonxConfig.logResponses()))
                    .version(watsonxConfig.version())
                    .spaceId(firstOrDefault(null, watsonxConfig.spaceId(), runtimeConfig.defaultConfig().spaceId()))
                    .projectId(firstOrDefault(null, watsonxConfig.projectId(), runtimeConfig.defaultConfig().projectId()))
                    .modelId(embeddingModelConfig.modelId())
                    .truncateInputTokens(embeddingModelConfig.truncateInputTokens().orElse(null));

            return new Supplier<>() {
                @Override
                public WatsonxEmbeddingModel get() {
                    return builder
                            .tokenGenerator(getOrCreateTokenGenerator(apiKey, iamBaseUrl, granType, duration))
                            .build();
                }
            };

        } else {
            return new Supplier<>() {
                @Override
                public EmbeddingModel get() {
                    return new DisabledEmbeddingModel();
                }

            };
        }
    }

    public Supplier<ScoringModel> scoringModel(
            LangChain4jWatsonxFixedRuntimeConfig fixedRuntimeConfig,
            LangChain4jWatsonxConfig runtimeConfig,
            String configName) {

        var watsonxFixedRuntimeConfig = correspondingWatsonxFixedRuntimeConfig(fixedRuntimeConfig, configName);
        var watsonxConfig = correspondingWatsonxRuntimeConfig(runtimeConfig, configName);
        var configProblems = checkConfigurations(fixedRuntimeConfig, runtimeConfig, configName);

        if (!configProblems.isEmpty()) {
            throw new ConfigValidationException(configProblems.toArray(EMPTY_PROBLEMS));
        }

        var apiKey = firstOrDefault(
                null,
                watsonxFixedRuntimeConfig.apiKey(),
                fixedRuntimeConfig.defaultConfig().apiKey());
        var iamBaseUrl = watsonxFixedRuntimeConfig.iam().baseUrl();
        var granType = watsonxFixedRuntimeConfig.iam().grantType();
        var duration = watsonxFixedRuntimeConfig.iam().timeout().orElse(Duration.ofSeconds(10));

        URL url;
        try {
            url = URI.create(
                    firstOrDefault(
                            null,
                            watsonxFixedRuntimeConfig.baseUrl(),
                            fixedRuntimeConfig.defaultConfig().baseUrl()))
                    .toURL();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ScoringModelConfig rerankModelConfig = watsonxConfig.scoringModel();
        var builder = WatsonxScoringModel.builder()
                .url(url)
                .timeout(watsonxConfig.timeout().orElse(Duration.ofSeconds(10)))
                .logRequests(firstOrDefault(false, rerankModelConfig.logRequests(), watsonxConfig.logRequests()))
                .logResponses(firstOrDefault(false, rerankModelConfig.logResponses(), watsonxConfig.logResponses()))
                .version(watsonxConfig.version())
                .spaceId(firstOrDefault(null, watsonxConfig.spaceId(), runtimeConfig.defaultConfig().spaceId()))
                .projectId(firstOrDefault(null, watsonxConfig.projectId(), runtimeConfig.defaultConfig().projectId()))
                .modelId(rerankModelConfig.modelId())
                .truncateInputTokens(rerankModelConfig.truncateInputTokens().orElse(null));

        return new Supplier<>() {
            @Override
            public WatsonxScoringModel get() {
                return builder
                        .tokenGenerator(getOrCreateTokenGenerator(apiKey, iamBaseUrl, granType, duration))
                        .build();
            }
        };
    }

    private WatsonxChatModel.Builder chatBuilder(LangChain4jWatsonxFixedRuntimeConfig fixedRuntimeConfig,
            LangChain4jWatsonxConfig runtimeConfig, String configName, String deploymentId) {

        var watsonxFixedRuntimeConfig = correspondingWatsonxFixedRuntimeConfig(fixedRuntimeConfig, configName);
        var watsonxRuntimeConfig = correspondingWatsonxRuntimeConfig(runtimeConfig, configName);
        var configProblems = checkConfigurations(fixedRuntimeConfig, runtimeConfig, configName);

        if (!configProblems.isEmpty()) {
            throw new ConfigValidationException(configProblems.toArray(EMPTY_PROBLEMS));
        }

        URL url;
        try {
            url = URI.create(
                    firstOrDefault(
                            null,
                            watsonxFixedRuntimeConfig.baseUrl(),
                            fixedRuntimeConfig.defaultConfig().baseUrl()))
                    .toURL();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ToolChoice toolChoice = null;
        String toolChoiceName = null;
        ChatModelConfig chatModelConfig = watsonxRuntimeConfig.chatModel();

        if (chatModelConfig.toolChoice().isPresent() && !chatModelConfig.toolChoice().get().isBlank()) {
            toolChoice = REQUIRED;
            toolChoiceName = chatModelConfig.toolChoice().get();
        }

        ResponseFormat responseFormat = null;
        if (chatModelConfig.responseFormat().isPresent()) {
            responseFormat = switch (chatModelConfig.responseFormat().get().toLowerCase()) {
                case "json_object" -> ResponseFormat.JSON;
                default -> throw new IllegalArgumentException(
                        "The value '%s' for the response-format property is not available. Use one of the values: [%s]"
                                .formatted(chatModelConfig.responseFormat().get(), "json_object"));
            };
        }

        return WatsonxChatModel.builder()
                .url(url)
                .timeout(watsonxRuntimeConfig.timeout().orElse(Duration.ofSeconds(10)))
                .logRequests(firstOrDefault(false, chatModelConfig.logRequests(), watsonxRuntimeConfig.logRequests()))
                .logResponses(firstOrDefault(false, chatModelConfig.logResponses(), watsonxRuntimeConfig.logResponses()))
                .version(watsonxRuntimeConfig.version())
                .spaceId(firstOrDefault(null, watsonxRuntimeConfig.spaceId(), runtimeConfig.defaultConfig().spaceId()))
                .projectId(firstOrDefault(null, watsonxRuntimeConfig.projectId(), runtimeConfig.defaultConfig().projectId()))
                .modelId(watsonxRuntimeConfig.chatModel().modelId())
                .toolChoice(toolChoice)
                .toolChoiceName(toolChoiceName)
                .frequencyPenalty(chatModelConfig.frequencyPenalty())
                .logprobs(chatModelConfig.logprobs())
                .topLogprobs(chatModelConfig.topLogprobs().orElse(null))
                .maxTokens(chatModelConfig.maxTokens())
                .n(chatModelConfig.n())
                .presencePenalty(chatModelConfig.presencePenalty())
                .seed(chatModelConfig.seed().orElse(null))
                .stop(chatModelConfig.stop().orElse(null))
                .temperature(chatModelConfig.temperature())
                .topP(chatModelConfig.topP())
                .responseFormat(responseFormat);
    }

    private WatsonxGenerationModel.Builder generationBuilder(
            LangChain4jWatsonxFixedRuntimeConfig fixedRuntimeConfig,
            LangChain4jWatsonxConfig runtimeConfig,
            String configName) {

        var watsonxFixedRuntimeConfig = correspondingWatsonxFixedRuntimeConfig(fixedRuntimeConfig, configName);
        var watsonxConfig = correspondingWatsonxRuntimeConfig(runtimeConfig, configName);
        var configProblems = checkConfigurations(fixedRuntimeConfig, runtimeConfig, configName);

        if (!configProblems.isEmpty()) {
            throw new ConfigValidationException(configProblems.toArray(EMPTY_PROBLEMS));
        }

        GenerationModelConfig generationModelConfig = watsonxConfig.generationModel();

        URL url;
        try {
            url = URI.create(
                    firstOrDefault(
                            null,
                            watsonxFixedRuntimeConfig.baseUrl(),
                            fixedRuntimeConfig.defaultConfig().baseUrl()))
                    .toURL();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Double decayFactor = generationModelConfig.lengthPenalty().decayFactor().orElse(null);
        Integer startIndex = generationModelConfig.lengthPenalty().startIndex().orElse(null);
        String promptJoiner = generationModelConfig.promptJoiner();

        return WatsonxGenerationModel.builder()
                .url(url)
                .timeout(watsonxConfig.timeout().orElse(Duration.ofSeconds(10)))
                .logRequests(firstOrDefault(false, generationModelConfig.logRequests(), watsonxConfig.logRequests()))
                .logResponses(firstOrDefault(false, generationModelConfig.logResponses(), watsonxConfig.logResponses()))
                .version(watsonxConfig.version())
                .spaceId(firstOrDefault(null, watsonxConfig.spaceId(), runtimeConfig.defaultConfig().spaceId()))
                .projectId(firstOrDefault(null, watsonxConfig.projectId(), runtimeConfig.defaultConfig().projectId()))
                .modelId(watsonxConfig.generationModel().modelId())
                .decodingMethod(generationModelConfig.decodingMethod())
                .decayFactor(decayFactor)
                .startIndex(startIndex)
                .maxNewTokens(generationModelConfig.maxNewTokens())
                .minNewTokens(generationModelConfig.minNewTokens())
                .temperature(generationModelConfig.temperature())
                .randomSeed(firstOrDefault(null, generationModelConfig.randomSeed()))
                .stopSequences(firstOrDefault(null, generationModelConfig.stopSequences()))
                .topK(firstOrDefault(null, generationModelConfig.topK()))
                .topP(firstOrDefault(null, generationModelConfig.topP()))
                .repetitionPenalty(firstOrDefault(null, generationModelConfig.repetitionPenalty()))
                .truncateInputTokens(generationModelConfig.truncateInputTokens().orElse(null))
                .includeStopSequence(generationModelConfig.includeStopSequence().orElse(null))
                .promptJoiner(promptJoiner);
    }

    private LangChain4jWatsonxFixedRuntimeConfig.WatsonxConfig correspondingWatsonxFixedRuntimeConfig(
            LangChain4jWatsonxFixedRuntimeConfig fixedRuntimeConfig,
            String configName) {

        LangChain4jWatsonxFixedRuntimeConfig.WatsonxConfig watsonxConfig;
        if (NamedConfigUtil.isDefault(configName)) {
            watsonxConfig = fixedRuntimeConfig.defaultConfig();
        } else {
            watsonxConfig = fixedRuntimeConfig.namedConfig().get(configName);
        }
        return watsonxConfig;
    }

    private LangChain4jWatsonxConfig.WatsonxConfig correspondingWatsonxRuntimeConfig(LangChain4jWatsonxConfig runtimeConfig,
            String configName) {
        LangChain4jWatsonxConfig.WatsonxConfig watsonxConfig;
        if (NamedConfigUtil.isDefault(configName)) {
            watsonxConfig = runtimeConfig.defaultConfig();
        } else {
            watsonxConfig = runtimeConfig.namedConfig().get(configName);
        }
        return watsonxConfig;
    }

    private List<ConfigValidationException.Problem> checkConfigurations(
            LangChain4jWatsonxFixedRuntimeConfig fixedRuntimeConfig,
            LangChain4jWatsonxConfig runtimeConfig,
            String configName) {

        var configProblems = new ArrayList<ConfigValidationException.Problem>();
        var watsonxFixedRuntimeConfig = correspondingWatsonxFixedRuntimeConfig(fixedRuntimeConfig, configName);
        var watsonxRuntimeConfig = correspondingWatsonxRuntimeConfig(runtimeConfig, configName);

        if (watsonxFixedRuntimeConfig.baseUrl().isEmpty() && fixedRuntimeConfig.defaultConfig().baseUrl().isEmpty()) {
            configProblems.add(createBaseURLConfigProblem(configName));
        }
        if (watsonxFixedRuntimeConfig.apiKey().isEmpty() && fixedRuntimeConfig.defaultConfig().apiKey().isEmpty()) {
            configProblems.add(createApiKeyConfigProblem(configName));
        }
        if (watsonxRuntimeConfig.projectId().isEmpty() && runtimeConfig.defaultConfig().projectId().isEmpty() &&
                watsonxRuntimeConfig.spaceId().isEmpty() && runtimeConfig.defaultConfig().spaceId().isEmpty()) {
            var config = NamedConfigUtil.isDefault(configName) ? "." : ("." + configName + ".");
            var errorMessage = "One of the properties quarkus.langchain4j.watsonx%s%s / quarkus.langchain4j.watsonx%s%s is required, but could not be found in any config source";
            configProblems.add(new ConfigValidationException.Problem(
                    String.format(errorMessage, config, "project-id", config, "space-id")));
        }

        return configProblems;
    }

    private ConfigValidationException.Problem createBaseURLConfigProblem(String configName) {
        return createConfigProblem("base-url", configName);
    }

    private ConfigValidationException.Problem createApiKeyConfigProblem(String configName) {
        return createConfigProblem("api-key", configName);
    }

    private static ConfigValidationException.Problem createConfigProblem(String key, String configName) {
        return new ConfigValidationException.Problem(String.format(
                "SRCFG00014: The config property quarkus.langchain4j.watsonx%s%s is required but it could not be found in any config source",
                NamedConfigUtil.isDefault(configName) ? "." : ("." + configName + "."), key));
    }
}
