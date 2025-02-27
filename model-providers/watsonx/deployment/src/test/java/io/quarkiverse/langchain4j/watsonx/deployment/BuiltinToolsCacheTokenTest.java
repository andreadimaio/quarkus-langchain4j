package io.quarkiverse.langchain4j.watsonx.deployment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.time.Instant;
import java.util.Date;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.github.tomakehurst.wiremock.stubbing.Scenario;

import io.quarkiverse.langchain4j.watsonx.tools.GoogleSearchTool;
import io.quarkiverse.langchain4j.watsonx.tools.WeatherTool;
import io.quarkiverse.langchain4j.watsonx.tools.WebCrawlerTool;
import io.quarkus.test.QuarkusUnitTest;

public class BuiltinToolsCacheTokenTest extends WireMockAbstract {

    static int cacheTimeout = 2000;
    static String RESPONSE_401 = """
            {
                "code": 401,
                "error": "Unauthorized",
                "reason": "Unauthorized",
                "message": "Access denied",
                "description": "jwt expired"
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
    WeatherTool weatherTool;

    @Test
    void try_weather_tool_retry() throws InterruptedException {

        var response = """
                {
                    "output": "Current weather in Naples:\\nTemperature: 12.1°C\\nRain: 0mm\\nRelative humidity: 94%\\nWind: 5km/h\\n"
                }""";

        mockIAMServer();
        mockWXServer(response);
        assertDoesNotThrow(() -> weatherTool.find("naples", null));
    }

    @Inject
    WebCrawlerTool webCrawlerTool;

    @Test
    void try_webcrawler_tool_retry() throws InterruptedException {

        String response = "{" +
                "\"output\": \"\\\"{\\\\\\\"url\\\\\\\":\\\\\\\"https://www.ibm.com/us-en\\\\\\\"," +
                "\\\\\\\"contentType\\\\\\\":\\\\\\\"text/html;charset=utf-8\\\\\\\"," +
                "\\\\\\\"content\\\\\\\":\\\\\\\"IBM - United States\\\\n\\\\nBoost developer productivity with AI..." +
                "\\\\n\\\\nExplore jobs\\\\n\\\\nStart learning\\\\\\\"}\\\"\"\n" +
                "}";

        mockIAMServer();
        mockWXServer(response);
        assertDoesNotThrow(() -> webCrawlerTool.process("http://supersite.com"));
    }

    @Inject
    GoogleSearchTool googleSearchTool;

    @Test
    void try_googlesearh_tool_retry() throws InterruptedException {

        String response = """
                {
                  "output": "[{\\"title\\":\\"Quarkus - Supersonic Subatomic Java\\",\\"description\\":\\"Quarkus streamlines framework optimizations in the build phase to reduce runtime dependencies and improve efficiency. By precomputing metadata and optimizing ...\\",\\"url\\":\\"https://quarkus.io/\\"}]"
                }
                """;

        mockIAMServer();
        mockWXServer(response);
        assertDoesNotThrow(() -> googleSearchTool.search("quarkus", 1));
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

    private void mockWXServer(String response) {
        mockServers.mockWxBuilder(WireMockUtil.URL_WX_AGENT_TOOL_RUN, 401)
                .token("expired_token")
                .scenario(Scenario.STARTED, "retry")
                .response(RESPONSE_401)
                .build();

        mockServers.mockWxBuilder(WireMockUtil.URL_WX_AGENT_TOOL_RUN, 200)
                .token("my_super_token")
                .scenario("retry", Scenario.STARTED)
                .response(response)
                .build();
    }
}
