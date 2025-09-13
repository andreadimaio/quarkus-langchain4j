package io.quarkiverse.langchain4j.watsonx.deployment;

import static io.quarkiverse.langchain4j.watsonx.deployment.WireMockUtil.API_KEY;
import static io.quarkiverse.langchain4j.watsonx.deployment.WireMockUtil.BEARER_TOKEN;
import static io.quarkiverse.langchain4j.watsonx.deployment.WireMockUtil.PROJECT_ID;
import static io.quarkiverse.langchain4j.watsonx.deployment.WireMockUtil.URL_IAM_SERVER;
import static io.quarkiverse.langchain4j.watsonx.deployment.WireMockUtil.URL_WATSONX_SERVER;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.Date;
import java.util.List;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.ibm.watsonx.ai.chat.model.ChatParameters;
import com.ibm.watsonx.ai.chat.model.ChatParameters.ToolChoice;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import io.quarkus.test.QuarkusUnitTest;

public class ChatAllPropertiesTest extends WireMockAbstract {

    @RegisterExtension
    static QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.base-url", URL_WATSONX_SERVER)
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.api-key", API_KEY)
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.space-id", "my-space-id")
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.project-id", PROJECT_ID)
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.timeout", "60s")
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.log-requests", "true")
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.log-responses", "true")
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.version", "aaaa-mm-dd")
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.iam.base-url", URL_IAM_SERVER)
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.iam.timeout", "60s")
            .overrideConfigKey("quarkus.langchain4j.watsonx.mode", "chat")
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.chat-model.frequency-penalty", "2")
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.chat-model.logprobs", "true")
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.chat-model.top-logprobs", "2")
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.chat-model.model-name", "my_super_model")
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.chat-model.max-tokens", "200")
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.chat-model.n", "2")
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.chat-model.presence-penalty", "2")
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.chat-model.seed", "41")
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.chat-model.stop", "word1,word2")
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.chat-model.temperature", "1.5")
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.chat-model.top-p", "0.5")
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.chat-model.response-format", "json_object")
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.chat-model.tool-choice", "required")
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.chat-model.tool-choice-name", "myfunction")
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.built-in-service.log-requests", "true")
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.built-in-service.log-responses", "true")
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class).addClass(WireMockUtil.class));

    @Override
    void handlerBeforeEach() {
        mockIAMBuilder(200)
                .grantType(langchain4jWatsonConfig.defaultConfig().iam().grantType())
                .response(BEARER_TOKEN, new Date())
                .build();
    }

    @Inject
    ChatModel chatModel;

    @Inject
    StreamingChatModel streamingChatModel;

    static ChatParameters parameters = ChatParameters.builder()
            .frequencyPenalty(2.0)
            .logprobs(true)
            .topLogprobs(2)
            .maxCompletionTokens(200)
            .presencePenalty(2.0)
            .seed(41)
            .stop(List.of("word1", "word2"))
            .temperature(1.5)
            .timeLimit(Duration.ofSeconds(60))
            .topP(0.5)
            .withJsonResponse()
            .toolChoice("myfunction")
            .build();

    @Test
    void check_config() throws Exception {
        var runtimeConfig = langchain4jWatsonConfig.defaultConfig();
        assertEquals(URL_WATSONX_SERVER, runtimeConfig.baseUrl().orElse(null).toString());
        assertEquals(URL_IAM_SERVER, runtimeConfig.iam().baseUrl().toString());
        assertEquals(API_KEY, runtimeConfig.apiKey().orElse(null));
        assertEquals("my-space-id", runtimeConfig.spaceId().orElse(null));
        assertEquals(PROJECT_ID, runtimeConfig.projectId().orElse(null));
        assertEquals(Duration.ofSeconds(60), runtimeConfig.timeout());
        assertEquals(Duration.ofSeconds(60), runtimeConfig.iam().timeout().get());
        assertEquals(true, runtimeConfig.logRequests().orElse(false));
        assertEquals(true, runtimeConfig.logResponses().orElse(false));
        assertEquals("aaaa-mm-dd", runtimeConfig.version());
        assertEquals("my_super_model", runtimeConfig.chatModel().modelName());
        assertEquals(2.0, runtimeConfig.chatModel().frequencyPenalty());
        assertEquals(true, runtimeConfig.chatModel().logprobs());
        assertEquals(2, runtimeConfig.chatModel().topLogprobs().orElse(null));
        assertEquals(200, runtimeConfig.chatModel().maxTokens());
        assertEquals(2, runtimeConfig.chatModel().n());
        assertEquals(2.0, runtimeConfig.chatModel().presencePenalty());
        assertEquals(41, runtimeConfig.chatModel().seed().orElse(null));
        assertEquals(List.of("word1", "word2"), runtimeConfig.chatModel().stop().orElse(null));
        assertEquals(1.5, runtimeConfig.chatModel().temperature());
        assertEquals(0.5, runtimeConfig.chatModel().topP());
        assertEquals("json_object", runtimeConfig.chatModel().responseFormat().orElse(null));
        assertEquals(ToolChoice.REQUIRED, runtimeConfig.chatModel().toolChoice().orElse(null));
        assertEquals("myfunction", runtimeConfig.chatModel().toolChoiceName().orElse(null));
        assertEquals(true, langchain4jWatsonConfig.builtInService().logRequests().orElse(false));
        assertEquals(true, langchain4jWatsonConfig.builtInService().logResponses().orElse(false));
    }
}
