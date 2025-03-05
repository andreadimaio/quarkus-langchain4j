package io.quarkiverse.langchain4j.watsonx.deployment;

import static io.quarkiverse.langchain4j.watsonx.deployment.WireMockUtil.API_KEY;
import static io.quarkiverse.langchain4j.watsonx.deployment.WireMockUtil.BEARER_TOKEN;
import static io.quarkiverse.langchain4j.watsonx.deployment.WireMockUtil.DEFAULT_TIME_LIMIT;
import static io.quarkiverse.langchain4j.watsonx.deployment.WireMockUtil.PROJECT_ID;
import static io.quarkiverse.langchain4j.watsonx.deployment.WireMockUtil.RESPONSE_WATSONX_CHAT_API;
import static io.quarkiverse.langchain4j.watsonx.deployment.WireMockUtil.URL_IAM_SERVER;
import static io.quarkiverse.langchain4j.watsonx.deployment.WireMockUtil.URL_WATSONX_CHAT_API;
import static io.quarkiverse.langchain4j.watsonx.deployment.WireMockUtil.URL_WATSONX_SERVER;
import static io.quarkiverse.langchain4j.watsonx.deployment.WireMockUtil.URL_WX_SERVER;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.ToolBox;
import io.quarkiverse.langchain4j.watsonx.bean.TextChatMessage;
import io.quarkiverse.langchain4j.watsonx.bean.TextChatMessage.TextChatMessageUser;
import io.quarkiverse.langchain4j.watsonx.bean.TextChatMessage.TextChatParameterTool;
import io.quarkiverse.langchain4j.watsonx.bean.TextChatParameters;
import io.quarkiverse.langchain4j.watsonx.bean.TextChatRequest;
import io.quarkiverse.langchain4j.watsonx.runtime.config.ChatModelConfig;
import io.quarkiverse.langchain4j.watsonx.runtime.config.LangChain4jWatsonxConfig;
import io.quarkiverse.langchain4j.watsonx.tools.GoogleSearchTool;
import io.quarkiverse.langchain4j.watsonx.tools.WeatherTool;
import io.quarkiverse.langchain4j.watsonx.tools.WebCrawlerTool;
import io.quarkus.test.QuarkusUnitTest;

@Disabled("The built-in tools can not be used directly as tools")
public class BuiltinToolsToolBoxTest extends WireMockAbstract {

    @RegisterExtension
    static QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.wx-base-url", URL_WX_SERVER)
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.base-url", URL_WATSONX_SERVER)
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.iam.base-url", URL_IAM_SERVER)
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.api-key", API_KEY)
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.project-id", PROJECT_ID)
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class).addClass(WireMockUtil.class));

    @Override
    void handlerBeforeEach() {
        mockIAMBuilder(200)
                .grantType(langchain4jWatsonConfig.defaultConfig().iam().grantType())
                .response(BEARER_TOKEN, new Date())
                .build();
    }

    @Singleton
    @RegisterAiService
    public static interface AiService {
        @ToolBox({ WebCrawlerTool.class, GoogleSearchTool.class, WeatherTool.class })
        public String chat(@UserMessage String userMessage);
    }

    @Inject
    AiService aiService;

    @Test
    void testBuiltinTool() throws Exception {

        var webCrawlerTool = TextChatParameterTool.of(
                ToolSpecification.builder()
                        .name("process")
                        .description("Fetches the content of a single web page")
                        .parameters(
                                JsonObjectSchema.builder()
                                        .addStringProperty("url", "URL for the webpage to be scraped")
                                        .required("url")
                                        .build())
                        .build());

        var googleSearchTool = TextChatParameterTool.of(
                ToolSpecification.builder()
                        .name("search")
                        .description(
                                "Search for online trends, news, current events, real-time information, or research topics")
                        .parameters(
                                JsonObjectSchema.builder()
                                        .addStringProperty("input", "Text to search")
                                        .addIntegerProperty("maxResults", "Max number of results to return")
                                        .required("input")
                                        .build())
                        .build());

        var weatherTool = TextChatParameterTool.of(
                ToolSpecification.builder()
                        .name("find")
                        .description("Find the weather for a city")
                        .parameters(
                                JsonObjectSchema.builder()
                                        .addStringProperty("name", "Name of the location")
                                        .addStringProperty("country", "Name of the state or country")
                                        .required("name")
                                        .build())
                        .build());

        List<TextChatParameterTool> tools = List.of(webCrawlerTool, googleSearchTool, weatherTool);
        List<TextChatMessage> messages = List.of(TextChatMessageUser.of("Hello"));

        mockWatsonxBuilder(URL_WATSONX_CHAT_API, 200)
                .bodyIgnoreOrder(mapper.writeValueAsString(generateChatRequest(messages, tools)))
                .response(RESPONSE_WATSONX_CHAT_API)
                .build();

        assertEquals("AI Response", aiService.chat("Hello"));
    }

    private TextChatRequest generateChatRequest(List<TextChatMessage> messages, List<TextChatParameterTool> tools) {
        LangChain4jWatsonxConfig.WatsonConfig watsonConfig = langchain4jWatsonConfig.defaultConfig();
        ChatModelConfig chatModelConfig = watsonConfig.chatModel();
        String modelId = chatModelConfig.modelId();
        String spaceId = watsonConfig.spaceId().orElse(null);
        String projectId = watsonConfig.projectId().orElse(null);

        TextChatParameters parameters = TextChatParameters.builder()
                .frequencyPenalty(0.0)
                .logprobs(false)
                .maxTokens(1024)
                .n(1)
                .presencePenalty(0.0)
                .temperature(1.0)
                .topP(1.0)
                .timeLimit(DEFAULT_TIME_LIMIT)
                .build();

        return new TextChatRequest(modelId, spaceId, projectId, messages, tools, parameters);
    }
}
