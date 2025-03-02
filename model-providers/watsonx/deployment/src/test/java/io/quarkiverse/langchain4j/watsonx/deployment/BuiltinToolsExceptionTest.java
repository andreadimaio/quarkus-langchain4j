package io.quarkiverse.langchain4j.watsonx.deployment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import jakarta.inject.Singleton;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.watsonx.tools.GoogleSearchTool;
import io.quarkiverse.langchain4j.watsonx.tools.WeatherTool;
import io.quarkiverse.langchain4j.watsonx.tools.WebCrawlerTool;
import io.quarkus.test.QuarkusUnitTest;

public class BuiltinToolsExceptionTest {

    @Singleton
    @RegisterAiService(tools = { WebCrawlerTool.class, GoogleSearchTool.class, WeatherTool.class })
    public static interface AiService {
        public String chat(@UserMessage String userMessage);
    }

    @Nested
    class WrongBaseUrl {
        @RegisterExtension
        static QuarkusUnitTest unitTest = new QuarkusUnitTest()
                .overrideConfigKey("quarkus.langchain4j.watsonx.base-url", WireMockUtil.URL_WATSONX_SERVER)
                .overrideConfigKey("quarkus.langchain4j.watsonx.iam.base-url", WireMockUtil.URL_IAM_SERVER)
                .overrideConfigKey("quarkus.langchain4j.watsonx.api-key", WireMockUtil.API_KEY)
                .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.project-id", WireMockUtil.PROJECT_ID)
                .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class).addClass(AiService.class))
                .assertException(t -> {
                    assertThat(t).isInstanceOf(RuntimeException.class)
                            .hasMessage(
                                    "The property 'quarkus.langchain4j.watsonx.base-url' does not have a correct url. Use one of the urls given in the documentation or use the property 'quarkus.langchain4j.watsonx.wx-base-url' to set a custom url for the agent-tool service");
                });

        @Test
        void test() {
            fail("Should not be called");
        }
    }

    @Nested
    class NoDefaultApiKey {
        @RegisterExtension
        static QuarkusUnitTest unitTest = new QuarkusUnitTest()
                .overrideConfigKey("quarkus.langchain4j.watsonx.base-url", "https://us-south.ml.cloud.ibm.com")
                .overrideConfigKey("quarkus.langchain4j.watsonx.iam.base-url", WireMockUtil.URL_IAM_SERVER)
                .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.project-id", WireMockUtil.PROJECT_ID)
                .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class).addClass(AiService.class))
                .assertException(t -> {
                    assertThat(t).isInstanceOf(RuntimeException.class)
                            .hasMessage(
                                    "To use the BuiltinToolClasses, you must set the property 'quarkus.langchain4j.watsonx.api-key'");
                });

        @Test
        void test() {
            fail("Should not be called");
        }
    }
}
