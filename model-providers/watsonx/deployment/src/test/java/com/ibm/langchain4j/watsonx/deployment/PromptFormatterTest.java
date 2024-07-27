package com.ibm.langchain4j.watsonx.deployment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.watsonx.bean.Parameters;
import io.quarkiverse.langchain4j.watsonx.bean.TextGenerationRequest;
import io.quarkiverse.langchain4j.watsonx.runtime.config.ChatModelConfig;
import io.quarkiverse.langchain4j.watsonx.runtime.config.LangChain4jWatsonxConfig;
import io.quarkus.test.QuarkusUnitTest;

public class PromptFormatterTest extends WireMockAbstract {

    @RegisterExtension
    static QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.base-url", WireMockUtil.URL_WATSONX_SERVER)
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.iam.base-url", WireMockUtil.URL_IAM_SERVER)
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.api-key", WireMockUtil.API_KEY)
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.project-id", WireMockUtil.PROJECT_ID)
            .overrideConfigKey("quarkus.langchain4j.watsonx.chat-model.model-id", "mistralai/mistral-large")
            .overrideConfigKey("quarkus.langchain4j.watsonx.chat-model.prompt-formatter", "true")
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class).addClass(WireMockUtil.class));

    @Override
    void handlerBeforeEach() {
        mockServers.mockIAMBuilder(200)
                .response("my_super_token", new Date())
                .build();
    }

    @RegisterAiService(chatMemoryProviderSupplier = RegisterAiService.NoChatMemoryProviderSupplier.class)
    @Singleton
    interface AIService {

        @SystemMessage("You are a poet")
        @UserMessage("Generate a poem about {topic}")
        String poem(String topic);
    }

    @RegisterAiService(chatMemoryProviderSupplier = RegisterAiService.NoChatMemoryProviderSupplier.class)
    @Singleton
    @SystemMessage("You are a poet")
    interface SystemMessageOnClassAIService {
        @UserMessage("Generate a poem about {topic}")
        String poem(String topic);
    }

    @Inject
    AIService aiService;

    @Inject
    SystemMessageOnClassAIService systemMessageOnClassAIService;

    @Test
    void ai_service_test() throws Exception {

        LangChain4jWatsonxConfig.WatsonConfig watsonConfig = langchain4jWatsonConfig.defaultConfig();
        ChatModelConfig chatModelConfig = watsonConfig.chatModel();
        String modelId = langchain4jWatsonFixedRuntimeConfig.defaultConfig().chatModel().modelId();
        String projectId = watsonConfig.projectId();

        Parameters parameters = Parameters.builder()
                .decodingMethod(chatModelConfig.decodingMethod())
                .temperature(chatModelConfig.temperature())
                .minNewTokens(chatModelConfig.minNewTokens())
                .maxNewTokens(chatModelConfig.maxNewTokens())
                .build();

        TextGenerationRequest body = new TextGenerationRequest(modelId, projectId,
                "<s>[INST] You are a poet [/INST]</s> [INST] Generate a poem about dog [/INST] ", parameters);

        mockServers.mockWatsonxBuilder(WireMockUtil.URL_WATSONX_CHAT_API, 200)
                .body(mapper.writeValueAsString(body))
                .response(WireMockUtil.RESPONSE_WATSONX_CHAT_API)
                .build();

        assertEquals("AI Response", aiService.poem("dog"));
    }

    @Test
    void system_message_on_class_test() throws Exception {

        LangChain4jWatsonxConfig.WatsonConfig watsonConfig = langchain4jWatsonConfig.defaultConfig();
        ChatModelConfig chatModelConfig = watsonConfig.chatModel();
        String modelId = langchain4jWatsonFixedRuntimeConfig.defaultConfig().chatModel().modelId();
        String projectId = watsonConfig.projectId();

        Parameters parameters = Parameters.builder()
                .decodingMethod(chatModelConfig.decodingMethod())
                .temperature(chatModelConfig.temperature())
                .minNewTokens(chatModelConfig.minNewTokens())
                .maxNewTokens(chatModelConfig.maxNewTokens())
                .build();

        TextGenerationRequest body = new TextGenerationRequest(modelId, projectId,
                "<s>[INST] You are a poet [/INST]</s> [INST] Generate a poem about dog [/INST] ", parameters);

        mockServers.mockWatsonxBuilder(WireMockUtil.URL_WATSONX_CHAT_API, 200)
                .body(mapper.writeValueAsString(body))
                .response(WireMockUtil.RESPONSE_WATSONX_CHAT_API)
                .build();

        assertEquals("AI Response", systemMessageOnClassAIService.poem("dog"));
    }
}
