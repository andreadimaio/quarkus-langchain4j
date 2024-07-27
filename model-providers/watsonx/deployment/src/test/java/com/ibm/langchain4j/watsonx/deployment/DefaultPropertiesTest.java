package com.ibm.langchain4j.watsonx.deployment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.TokenCountEstimator;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import io.quarkiverse.langchain4j.watsonx.WatsonxChatModel;
import io.quarkiverse.langchain4j.watsonx.bean.EmbeddingRequest;
import io.quarkiverse.langchain4j.watsonx.bean.Parameters;
import io.quarkiverse.langchain4j.watsonx.bean.TextGenerationRequest;
import io.quarkiverse.langchain4j.watsonx.bean.TokenizationRequest;
import io.quarkiverse.langchain4j.watsonx.prompt.GranitePromptFormatter;
import io.quarkus.arc.ClientProxy;
import io.quarkus.test.QuarkusUnitTest;

public class DefaultPropertiesTest extends WireMockAbstract {

    @RegisterExtension
    static QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.base-url", WireMockUtil.URL_WATSONX_SERVER)
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.iam.base-url", WireMockUtil.URL_IAM_SERVER)
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.api-key", WireMockUtil.API_KEY)
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.project-id", WireMockUtil.PROJECT_ID)
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class).addClass(WireMockUtil.class));

    @Override
    void handlerBeforeEach() {
        mockServers.mockIAMBuilder(200)
                .response("my_super_token", new Date())
                .build();
    }

    static Parameters parameters = Parameters.builder()
            .minNewTokens(0)
            .maxNewTokens(200)
            .decodingMethod("greedy")
            .temperature(1.0)
            .build();

    @Inject
    ChatLanguageModel chatModel;

    @Inject
    StreamingChatLanguageModel streamingChatModel;

    @Inject
    EmbeddingModel embeddingModel;

    @Inject
    TokenCountEstimator tokenCountEstimator;

    @Test
    void prompt_formatter() {
        var unwrapChatModel = (WatsonxChatModel) ClientProxy.unwrap(chatModel);
        unwrapChatModel.getPromptFormatter().instanceOf(GranitePromptFormatter.class);
    }

    @Test
    void check_config() throws Exception {
        var config = langchain4jWatsonConfig.defaultConfig();
        assertEquals(Optional.empty(), config.timeout());
        assertEquals(Optional.empty(), config.iam().timeout());
        assertEquals(false, config.logRequests().orElse(false));
        assertEquals(false, config.logResponses().orElse(false));
        assertEquals(WireMockUtil.VERSION, config.version());
        assertEquals(WireMockUtil.DEFAULT_CHAT_MODEL,
                langchain4jWatsonFixedRuntimeConfig.defaultConfig().chatModel().modelId());
        assertEquals("greedy", config.chatModel().decodingMethod());
        assertEquals(null, config.chatModel().lengthPenalty().decayFactor().orElse(null));
        assertEquals(null, config.chatModel().lengthPenalty().startIndex().orElse(null));
        assertEquals(200, config.chatModel().maxNewTokens());
        assertEquals(0, config.chatModel().minNewTokens());
        assertEquals(null, config.chatModel().randomSeed().orElse(null));
        assertEquals(null, config.chatModel().stopSequences().orElse(null));
        assertEquals(1.0, config.chatModel().temperature());
        assertEquals("\n", config.chatModel().promptJoiner());
        assertTrue(config.chatModel().topK().isEmpty());
        assertTrue(config.chatModel().topP().isEmpty());
        assertTrue(config.chatModel().repetitionPenalty().isEmpty());
        assertTrue(config.chatModel().truncateInputTokens().isEmpty());
        assertTrue(config.chatModel().includeStopSequence().isEmpty());
        assertEquals("urn:ibm:params:oauth:grant-type:apikey", config.iam().grantType());
        assertEquals(WireMockUtil.DEFAULT_EMBEDDING_MODEL, config.embeddingModel().modelId());
    }

    @Test
    void check_chat_model_config() throws Exception {
        var config = langchain4jWatsonConfig.defaultConfig();
        String modelId = langchain4jWatsonFixedRuntimeConfig.defaultConfig().chatModel().modelId();
        String projectId = config.projectId();

        TextGenerationRequest body = new TextGenerationRequest(modelId, projectId, "SystemMessage\nUserMessage", parameters);

        mockServers.mockWatsonxBuilder(WireMockUtil.URL_WATSONX_CHAT_API, 200)
                .body(mapper.writeValueAsString(body))
                .response(WireMockUtil.RESPONSE_WATSONX_CHAT_API)
                .build();

        assertEquals("AI Response", chatModel.generate(dev.langchain4j.data.message.SystemMessage.from("SystemMessage"),
                dev.langchain4j.data.message.UserMessage.from("UserMessage")).content().text());
    }

    @Test
    void check_embedding_model() throws Exception {
        var config = langchain4jWatsonConfig.defaultConfig();
        String modelId = config.embeddingModel().modelId();
        String projectId = config.projectId();

        EmbeddingRequest request = new EmbeddingRequest(modelId, projectId,
                List.of("Embedding THIS!"));

        mockServers.mockWatsonxBuilder(WireMockUtil.URL_WATSONX_EMBEDDING_API, 200)
                .body(mapper.writeValueAsString(request))
                .response(WireMockUtil.RESPONSE_WATSONX_EMBEDDING_API.formatted(modelId))
                .build();

        Response<Embedding> response = embeddingModel.embed("Embedding THIS!");
        assertNotNull(response);
        assertNotNull(response.content());
    }

    @Test
    void check_token_count_estimator() throws Exception {
        var config = langchain4jWatsonConfig.defaultConfig();
        String modelId = langchain4jWatsonFixedRuntimeConfig.defaultConfig().chatModel().modelId();
        String projectId = config.projectId();

        var body = new TokenizationRequest(modelId, "test", projectId);

        mockServers.mockWatsonxBuilder(WireMockUtil.URL_WATSONX_TOKENIZER_API, 200)
                .body(mapper.writeValueAsString(body))
                .response(WireMockUtil.RESPONSE_WATSONX_TOKENIZER_API.formatted(modelId))
                .build();

        assertEquals(11, tokenCountEstimator.estimateTokenCount("test"));
    }

    @Test
    void check_chat_streaming_model_config() throws Exception {
        var config = langchain4jWatsonConfig.defaultConfig();
        String modelId = langchain4jWatsonFixedRuntimeConfig.defaultConfig().chatModel().modelId();
        String projectId = config.projectId();

        TextGenerationRequest body = new TextGenerationRequest(modelId, projectId, "SystemMessage\nUserMessage", parameters);

        mockServers.mockWatsonxBuilder(WireMockUtil.URL_WATSONX_CHAT_STREAMING_API, 200)
                .body(mapper.writeValueAsString(body))
                .responseMediaType(MediaType.SERVER_SENT_EVENTS)
                .response(WireMockUtil.RESPONSE_WATSONX_STREAMING_API)
                .build();

        var messages = List.of(
                dev.langchain4j.data.message.SystemMessage.from("SystemMessage"),
                dev.langchain4j.data.message.UserMessage.from("UserMessage"));

        var streamingResponse = new AtomicReference<AiMessage>();
        streamingChatModel.generate(messages, WireMockUtil.streamingResponseHandler(streamingResponse));

        await().atMost(Duration.ofMinutes(1))
                .pollInterval(Duration.ofSeconds(2))
                .until(() -> streamingResponse.get() != null);

        assertThat(streamingResponse.get().text())
                .isNotNull()
                .isEqualTo(". I'm a beginner");
    }
}
