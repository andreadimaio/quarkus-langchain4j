package io.quarkiverse.langchain4j.ai.runtime.gemini;

import static dev.langchain4j.data.message.AiMessage.aiMessage;
import static dev.langchain4j.internal.Utils.getOrDefault;
import static dev.langchain4j.model.chat.Capability.RESPONSE_FORMAT_JSON_SCHEMA;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.jboss.resteasy.reactive.client.api.LoggingScope;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ResponseFormatType;
import dev.langchain4j.model.chat.request.json.JsonEnumSchema;
import dev.langchain4j.model.output.Response;
import io.quarkiverse.langchain4j.gemini.common.ContentMapper;
import io.quarkiverse.langchain4j.gemini.common.FinishReasonMapper;
import io.quarkiverse.langchain4j.gemini.common.GenerateContentRequest;
import io.quarkiverse.langchain4j.gemini.common.GenerateContentResponse;
import io.quarkiverse.langchain4j.gemini.common.GenerateContentResponseHandler;
import io.quarkiverse.langchain4j.gemini.common.GenerationConfig;
import io.quarkiverse.langchain4j.gemini.common.SchemaMapper;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;

public class AiGeminiChatLanguageModel implements ChatLanguageModel {

    private final AiGeminiRestApi.ApiMetadata apiMetadata;
    private final AiGeminiRestApi restApi;

    private final Double temperature;
    private final Integer maxOutputTokens;
    private final Integer topK;
    private final Double topP;
    private final ResponseFormat responseFormat;

    private AiGeminiChatLanguageModel(Builder builder) {
        this.temperature = builder.temperature;
        this.maxOutputTokens = builder.maxOutputTokens;
        this.topK = builder.topK;
        this.topP = builder.topP;
        this.responseFormat = builder.responseFormat;

        this.apiMetadata = AiGeminiRestApi.ApiMetadata
                .builder()
                .modelId(builder.modelId)
                .key(builder.key)
                .build();

        try {
            String baseUrl = builder.baseUrl.orElse("https://generativelanguage.googleapis.com");
            var restApiBuilder = QuarkusRestClientBuilder.newBuilder()
                    .baseUri(new URI(baseUrl))
                    .connectTimeout(builder.timeout.toSeconds(), TimeUnit.SECONDS)
                    .readTimeout(builder.timeout.toSeconds(), TimeUnit.SECONDS);

            if (builder.logRequests || builder.logResponses) {
                restApiBuilder.loggingScope(LoggingScope.REQUEST_RESPONSE);
                restApiBuilder.clientLogger(new AiGeminiRestApi.AiClientLogger(builder.logRequests,
                        builder.logResponses));
            }
            restApi = restApiBuilder.build(AiGeminiRestApi.class);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<Capability> supportedCapabilities() {
        Set<Capability> capabilities = new HashSet<>();
        // when response format is not null, it's JSON, either application/json or text/x.enum
        if (this.responseFormat != null && ResponseFormatType.JSON.equals(this.responseFormat.type())) {
            capabilities.add(RESPONSE_FORMAT_JSON_SCHEMA);
        } else if (responseFormat == null) { // when the responseFormat is not set, we default to supporting JSON as Google's models support this
            capabilities.add(RESPONSE_FORMAT_JSON_SCHEMA);
        }
        return capabilities;
    }

    @Override
    public dev.langchain4j.model.chat.response.ChatResponse chat(dev.langchain4j.model.chat.request.ChatRequest chatRequest) {
        ChatRequestParameters requestParameters = chatRequest.parameters();
        ResponseFormat effectiveResponseFormat = getOrDefault(requestParameters.responseFormat(), responseFormat);
        GenerationConfig generationConfig = GenerationConfig.builder()
                .maxOutputTokens(getOrDefault(requestParameters.maxOutputTokens(), this.maxOutputTokens))
                .responseMimeType(computeMimeType(effectiveResponseFormat))
                .responseSchema(effectiveResponseFormat != null
                        ? SchemaMapper.fromJsonSchemaToSchema(effectiveResponseFormat.jsonSchema())
                        : null)
                .stopSequences(requestParameters.stopSequences())
                .temperature(getOrDefault(requestParameters.temperature(), this.temperature))
                .topK(getOrDefault(requestParameters.topK(), this.topK))
                .topP(getOrDefault(requestParameters.topP(), this.topP))
                .build();
        GenerateContentRequest request = ContentMapper.map(chatRequest.messages(), chatRequest.toolSpecifications(),
                generationConfig);

        GenerateContentResponse response = restApi.generateContent(request, apiMetadata);

        String text = GenerateContentResponseHandler.getText(response);
        List<ToolExecutionRequest> toolExecutionRequests = GenerateContentResponseHandler.getToolExecutionRequests(response);
        AiMessage aiMessage = toolExecutionRequests.isEmpty()
                ? aiMessage(text)
                : aiMessage(text, toolExecutionRequests);
        return dev.langchain4j.model.chat.response.ChatResponse.builder()
                .aiMessage(aiMessage)
                .tokenUsage(GenerateContentResponseHandler.getTokenUsage(response.usageMetadata()))
                .finishReason(FinishReasonMapper.map(GenerateContentResponseHandler.getFinishReason(response)))
                .build();
    }

    private String computeMimeType(ResponseFormat responseFormat) {
        if (responseFormat == null || ResponseFormatType.TEXT.equals(responseFormat.type())) {
            return "text/plain";
        }

        if (ResponseFormatType.JSON.equals(responseFormat.type()) &&
                responseFormat.jsonSchema() != null &&
                responseFormat.jsonSchema().rootElement() != null &&
                responseFormat.jsonSchema().rootElement() instanceof JsonEnumSchema) {
            return "text/x.enum";
        }

        return "application/json";
    }

    @Override
    public Response<AiMessage> generate(List<ChatMessage> messages, List<ToolSpecification> toolSpecifications) {
        var chatResponse = chat(dev.langchain4j.model.chat.request.ChatRequest.builder()
                .messages(messages)
                .toolSpecifications(toolSpecifications)
                .build());

        return Response.from(
                chatResponse.aiMessage(),
                chatResponse.tokenUsage(),
                chatResponse.finishReason());

    }

    @Override
    public Response<AiMessage> generate(List<ChatMessage> messages) {
        return generate(messages, Collections.emptyList());
    }

    @Override
    public Response<AiMessage> generate(List<ChatMessage> messages, ToolSpecification toolSpecification) {
        return generate(messages,
                toolSpecification != null ? Collections.singletonList(toolSpecification) : Collections.emptyList());
    }

    public static Builder builder() {
        return new Builder();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static final class Builder {

        private Optional<String> baseUrl = Optional.empty();
        private String modelId;
        private String key;
        private Double temperature;
        private Integer maxOutputTokens;
        private Integer topK;
        private Double topP;
        private ResponseFormat responseFormat;
        private Duration timeout = Duration.ofSeconds(10);
        private Boolean logRequests = false;
        private Boolean logResponses = false;

        public Builder baseUrl(Optional<String> baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder modelId(String modelId) {
            this.modelId = modelId;
            return this;
        }

        public Builder temperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder maxOutputTokens(Integer maxOutputTokens) {
            this.maxOutputTokens = maxOutputTokens;
            return this;
        }

        public Builder topK(Integer topK) {
            this.topK = topK;
            return this;
        }

        public Builder topP(Double topP) {
            this.topP = topP;
            return this;
        }

        public Builder responseFormat(ResponseFormat responseFormat) {
            this.responseFormat = responseFormat;
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder logRequests(boolean logRequests) {
            this.logRequests = logRequests;
            return this;
        }

        public Builder logResponses(boolean logResponses) {
            this.logResponses = logResponses;
            return this;
        }

        public AiGeminiChatLanguageModel build() {
            return new AiGeminiChatLanguageModel(this);
        }
    }
}
