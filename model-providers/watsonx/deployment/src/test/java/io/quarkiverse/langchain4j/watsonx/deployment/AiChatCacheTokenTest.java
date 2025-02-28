package io.quarkiverse.langchain4j.watsonx.deployment;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.tomakehurst.wiremock.stubbing.Scenario;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import io.quarkiverse.langchain4j.watsonx.WatsonxChatRequestParameters;
import io.quarkiverse.langchain4j.watsonx.deployment.WireMockUtil.WatsonxBuilder;
import io.quarkus.test.QuarkusUnitTest;

public class AiChatCacheTokenTest extends WireMockAbstract {

    static int cacheTimeout = 2000;
    static String CHAT_RESPONSE_401 = """
            {
                "errors": [
                    {
                        "code": "authentication_token_expired",
                        "message": "Failed to authenticate the request due to an expired token",
                        "more_info": "https://cloud.ibm.com/apidocs/watsonx-ai"
                    }
                ],
                "trace": "fc4afd38813180730e10a5a3d56c1f25",
                "status_code": 401
            }
            """;

    static String AGENT_LAB_CHAT_RESPONSE_401 = """
            {
                "trace": "f41f4579293a4e14a9afb8d482690167",
                "errors": [
                    {
                        "code": "unauthorized",
                        "message": "Failed to authenticate the request due to an expired token."
                    }
                ],
                "status_code": 401
            }
            """;

    @RegisterExtension
    static QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .overrideConfigKey("quarkus.langchain4j.watsonx.base-url", WireMockUtil.URL_WATSONX_SERVER)
            .overrideConfigKey("quarkus.langchain4j.watsonx.wx-base-url", WireMockUtil.URL_WX_SERVER)
            .overrideConfigKey("quarkus.langchain4j.watsonx.iam.base-url", WireMockUtil.URL_IAM_SERVER)
            .overrideConfigKey("quarkus.langchain4j.watsonx.api-key", WireMockUtil.API_KEY)
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.project-id", WireMockUtil.PROJECT_ID)
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class).addClass(WireMockUtil.class));

    @AfterEach
    void afterEach() throws Exception {
        Thread.sleep(cacheTimeout);
    }

    @Inject
    ChatLanguageModel chatModel;

    @Inject
    StreamingChatLanguageModel streamingChatModel;

    @Override
    void handlerBeforeEach() throws Exception {
        Thread.sleep(cacheTimeout);
    }

    @Test
    void try_token_cache() throws InterruptedException {

        Date date = Date.from(Instant.now().plusMillis(cacheTimeout));

        // First call returns 200.
        mockServers.mockIAMBuilder(200)
                .scenario(Scenario.STARTED, "error")
                .response("3secondstoken", date)
                .build();

        // All other call after 2 seconds they will give an error.
        mockServers.mockIAMBuilder(500)
                .scenario("error", Scenario.STARTED)
                .response("Should never happen")
                .build();

        Stream.of(
                Map.entry(WireMockUtil.URL_WATSONX_CHAT_API, WireMockUtil.RESPONSE_WATSONX_CHAT_API),
                Map.entry(WireMockUtil.URL_WATSONX_CHAT_AGENT_LAB_API,
                        WireMockUtil.RESPONSE_WATSONX_AGENT_LAB_CHAT_API),
                Map.entry(WireMockUtil.URL_WATSONX_CHAT_STREAMING_API,
                        WireMockUtil.RESPONSE_WATSONX_CHAT_STREAMING_API)
        // TODO: ADD AGENT_LAB_STREAMING
        )
                .forEach(entry -> {

                    WatsonxBuilder builder;
                    if (entry.getKey().equals(WireMockUtil.URL_WATSONX_CHAT_AGENT_LAB_API)
                            || entry.getKey().equals(WireMockUtil.URL_WATSONX_CHAT_STREAMING_AGENT_LAB_API)) {
                        builder = mockServers.mockWatsonxBuilder(entry.getKey(), 200,
                                WireMockUtil.DEPLOYMENT, WireMockUtil.VERSION, WireMockUtil.PROJECT_ID);
                    } else {
                        builder = mockServers.mockWatsonxBuilder(entry.getKey(), 200);
                    }

                    builder.token("3secondstoken")
                            .responseMediaType(entry.getKey().equals(WireMockUtil.URL_WATSONX_CHAT_STREAMING_API)
                                    ? MediaType.SERVER_SENT_EVENTS
                                    : MediaType.APPLICATION_JSON)
                            .response(entry.getValue())
                            .build();

                    switch (entry.getKey()) {
                        case WireMockUtil.URL_WATSONX_CHAT_API -> assertDoesNotThrow(() -> chatModel.generate("message"));
                        case WireMockUtil.URL_WATSONX_CHAT_STREAMING_API -> {
                            var streamingResponse = new AtomicReference<AiMessage>();
                            assertDoesNotThrow(() -> streamingChatModel.generate("message",
                                    WireMockUtil.streamingResponseHandler(streamingResponse)));
                            await().atMost(Duration.ofSeconds(6))
                                    .pollInterval(Duration.ofSeconds(2))
                                    .until(() -> streamingResponse.get() != null);
                            assertNotNull(streamingResponse.get()); // cache
                        }
                        case WireMockUtil.URL_WATSONX_CHAT_AGENT_LAB_API -> {
                            var parameters = WatsonxChatRequestParameters.builder().deploymentId(WireMockUtil.DEPLOYMENT)
                                    .build();
                            assertDoesNotThrow(() -> chatModel.chat(
                                    ChatRequest.builder()
                                            .messages(UserMessage.from("test"))
                                            .parameters(parameters)
                                            .build())); // cache
                        }
                    }
                });
    }

    @Test
    void try_chat_api_token_retry() throws InterruptedException {

        mockIAMServer();

        Stream.of(
                Map.entry(WireMockUtil.URL_WATSONX_CHAT_API, WireMockUtil.RESPONSE_WATSONX_CHAT_API),
                Map.entry(WireMockUtil.URL_WATSONX_CHAT_STREAMING_API,
                        WireMockUtil.RESPONSE_WATSONX_CHAT_STREAMING_API))
                .forEach(entry -> {
                    mockServers.mockWatsonxBuilder(entry.getKey(), 401)
                            .token("expired_token")
                            .scenario(Scenario.STARTED, "retry")
                            .response(CHAT_RESPONSE_401)
                            .build();

                    mockServers.mockWatsonxBuilder(entry.getKey(), 200)
                            .token("my_super_token")
                            .scenario("retry", Scenario.STARTED)
                            .responseMediaType(entry.getKey().equals(WireMockUtil.URL_WATSONX_CHAT_STREAMING_API)
                                    ? MediaType.SERVER_SENT_EVENTS
                                    : MediaType.APPLICATION_JSON)
                            .response(entry.getValue())
                            .build();

                    switch (entry.getKey()) {
                        case WireMockUtil.URL_WATSONX_CHAT_API -> assertDoesNotThrow(() -> chatModel.generate("message"));
                        case WireMockUtil.URL_WATSONX_CHAT_STREAMING_API -> {
                            var streamingResponse = new AtomicReference<AiMessage>();
                            assertDoesNotThrow(() -> streamingChatModel.generate("message",
                                    WireMockUtil.streamingResponseHandler(streamingResponse)));
                            await().atMost(Duration.ofSeconds(6))
                                    .pollInterval(Duration.ofSeconds(2))
                                    .until(() -> streamingResponse.get() != null);
                            assertNotNull(streamingResponse.get());
                        }
                    }
                    try {
                        Thread.sleep(cacheTimeout);
                    } catch (InterruptedException e) {
                        fail(e);
                    }
                });
    }

    @Test
    void try_agent_lab_api_token_retry() throws InterruptedException {

        mockIAMServer();

        mockServers.mockWatsonxBuilder(WireMockUtil.URL_WATSONX_CHAT_AGENT_LAB_API, 401,
                WireMockUtil.DEPLOYMENT, WireMockUtil.VERSION, WireMockUtil.PROJECT_ID)
                .token("expired_token")
                .scenario(Scenario.STARTED, "retry")
                .response(AGENT_LAB_CHAT_RESPONSE_401)
                .build();

        mockServers.mockWatsonxBuilder(WireMockUtil.URL_WATSONX_CHAT_AGENT_LAB_API, 200,
                WireMockUtil.DEPLOYMENT, WireMockUtil.VERSION, WireMockUtil.PROJECT_ID)
                .token("my_super_token")
                .scenario("retry", Scenario.STARTED)
                .response(WireMockUtil.RESPONSE_WATSONX_AGENT_LAB_CHAT_API)
                .build();

        assertDoesNotThrow(() -> chatModel.chat(
                ChatRequest.builder()
                        .messages(UserMessage.from("test"))
                        .parameters(WatsonxChatRequestParameters.builder().deploymentId(WireMockUtil.DEPLOYMENT).build())
                        .build()));
    }

    private void mockIAMServer() {
        // Return an expired token.
        mockServers.mockIAMBuilder(200)
                .scenario(Scenario.STARTED, "retry")
                .response("expired_token", Date.from(Instant.now().minusSeconds(3)))
                .build();

        // Second call (retryOn) returns 200
        mockServers.mockIAMBuilder(200)
                .scenario("retry", Scenario.STARTED)
                .response("my_super_token", Date.from(Instant.now().plusMillis(cacheTimeout)))
                .build();
    }
}
