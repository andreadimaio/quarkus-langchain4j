package io.quarkiverse.langchain4j.watsonx.deployment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import jakarta.inject.Singleton;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.ToolBox;
import io.quarkiverse.langchain4j.watsonx.AgentLab;
import io.quarkus.test.QuarkusUnitTest;

public class AgentLabExceptionTest {

    @Singleton
    public static class MyTool {
        @Tool
        public int sum(int firstNumber, int secondNumber) {
            return firstNumber + secondNumber;
        }
    }

    @Singleton
    @RegisterAiService
    @AgentLab("")
    public static interface EmptyDeploymentIdAiService {
        public String chat(String userMessage);
    }

    @Singleton
    @RegisterAiService
    @AgentLab("7e354c85-d195-4dca-b53e-9566ae9f535b")
    public static interface GenerationAiService {
        public String chat(String userMessage);
    }

    @Singleton
    @RegisterAiService
    @SystemMessage("My systemMessage")
    @AgentLab("7e354c85-d195-4dca-b53e-9566ae9f535b")
    public static interface SystemMessageClassAiService {
        public String chat(@UserMessage String userMessage);
    }

    @Singleton
    @RegisterAiService
    @AgentLab("7e354c85-d195-4dca-b53e-9566ae9f535b")
    public static interface SystemMessageMethodAiService {
        @SystemMessage("My systemMessage")
        public String chat(@UserMessage String userMessage);
    }

    @Singleton
    @RegisterAiService
    @AgentLab("7e354c85-d195-4dca-b53e-9566ae9f535b")
    public static interface ToolBoxAiService {
        @ToolBox(MyTool.class)
        public String chat(@UserMessage String userMessage);
    }

    @Singleton
    @RegisterAiService(tools = MyTool.class)
    @AgentLab("7e354c85-d195-4dca-b53e-9566ae9f535b")
    public static interface ToolsAiService {
        public String chat(@UserMessage String userMessage);
    }

    @Nested
    class EmptyDeploymentIdTest {
        @RegisterExtension
        static QuarkusUnitTest unitTest = new QuarkusUnitTest()
                .overrideConfigKey("quarkus.langchain4j.watsonx.base-url", WireMockUtil.URL_WATSONX_SERVER)
                .overrideConfigKey("quarkus.langchain4j.watsonx.iam.base-url", WireMockUtil.URL_IAM_SERVER)
                .overrideConfigKey("quarkus.langchain4j.watsonx.api-key", WireMockUtil.API_KEY)
                .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.project-id", WireMockUtil.PROJECT_ID)
                .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class).addClass(EmptyDeploymentIdAiService.class))
                .assertException(t -> {
                    assertThat(t).isInstanceOf(RuntimeException.class)
                            .hasMessage(
                                    "The @AgentLab cannot have a null or empty value. Offending class is %s"
                                            .formatted(EmptyDeploymentIdAiService.class.getName()));
                });

        @Test
        void test() {
            fail("Should not be called");
        }
    }

    @Nested
    class AgentLabWithGenerationModeTest {
        @RegisterExtension
        static QuarkusUnitTest unitTest = new QuarkusUnitTest()
                .overrideConfigKey("quarkus.langchain4j.watsonx.base-url", WireMockUtil.URL_WATSONX_SERVER)
                .overrideConfigKey("quarkus.langchain4j.watsonx.iam.base-url", WireMockUtil.URL_IAM_SERVER)
                .overrideConfigKey("quarkus.langchain4j.watsonx.api-key", WireMockUtil.API_KEY)
                .overrideConfigKey("quarkus.langchain4j.watsonx.mode", "generation")
                .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.project-id", WireMockUtil.PROJECT_ID)
                .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class).addClass(GenerationAiService.class))
                .assertException(t -> {
                    assertThat(t).isInstanceOf(RuntimeException.class)
                            .hasMessage(
                                    "The @AgentLab can only be used in chat mode. Change the quarkus.langchain4j.watsonx.mode property from \"generation\" to \"chat\"");
                });

        @Test
        void test() {
            fail("Should not be called");
        }
    }

    @Nested
    class SystemMessageOnClassTest {
        @RegisterExtension
        static QuarkusUnitTest unitTest = new QuarkusUnitTest()
                .overrideConfigKey("quarkus.langchain4j.watsonx.base-url", WireMockUtil.URL_WATSONX_SERVER)
                .overrideConfigKey("quarkus.langchain4j.watsonx.iam.base-url", WireMockUtil.URL_IAM_SERVER)
                .overrideConfigKey("quarkus.langchain4j.watsonx.api-key", WireMockUtil.API_KEY)
                .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.project-id", WireMockUtil.PROJECT_ID)
                .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class).addClass(SystemMessageClassAiService.class))
                .assertException(t -> {
                    assertThat(t).isInstanceOf(RuntimeException.class)
                            .hasMessage(
                                    "You cannot use the @SystemMessage annotation in conjunction with the use of @AgentLab. The content of the @SystemMessage must be configured in the agent created in watsonx.ai agent lab UI. Offending class is %s"
                                            .formatted(SystemMessageClassAiService.class.getName()));
                });

        @Test
        void test() {
            fail("Should not be called");
        }
    }

    @Nested
    class SystemMessageOnMethodTest {
        @RegisterExtension
        static QuarkusUnitTest unitTest = new QuarkusUnitTest()
                .overrideConfigKey("quarkus.langchain4j.watsonx.base-url", WireMockUtil.URL_WATSONX_SERVER)
                .overrideConfigKey("quarkus.langchain4j.watsonx.iam.base-url", WireMockUtil.URL_IAM_SERVER)
                .overrideConfigKey("quarkus.langchain4j.watsonx.api-key", WireMockUtil.API_KEY)
                .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.project-id", WireMockUtil.PROJECT_ID)
                .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class).addClass(SystemMessageMethodAiService.class))
                .assertException(t -> {
                    assertThat(t).isInstanceOf(RuntimeException.class)
                            .hasMessage(
                                    "You cannot use the @SystemMessage annotation in conjunction with the use of @AgentLab. The content of the @SystemMessage must be configured in the agent created in watsonx.ai agent lab UI. Offending class is %s"
                                            .formatted(SystemMessageMethodAiService.class.getName()));
                });

        @Test
        void test() {
            fail("Should not be called");
        }
    }

    @Nested
    class ToolBoxTest {
        @RegisterExtension
        static QuarkusUnitTest unitTest = new QuarkusUnitTest()
                .overrideConfigKey("quarkus.langchain4j.watsonx.base-url", WireMockUtil.URL_WATSONX_SERVER)
                .overrideConfigKey("quarkus.langchain4j.watsonx.iam.base-url", WireMockUtil.URL_IAM_SERVER)
                .overrideConfigKey("quarkus.langchain4j.watsonx.api-key", WireMockUtil.API_KEY)
                .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.project-id", WireMockUtil.PROJECT_ID)
                .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class).addClasses(ToolBoxAiService.class, MyTool.class))
                .assertException(t -> {
                    assertThat(t).isInstanceOf(RuntimeException.class)
                            .hasMessage(
                                    "You cannot use tools in conjunction with the use of @AgentLab. The content of the tools must be configured in the agent created in watsonx.ai agent lab UI. Offending class is %s"
                                            .formatted(ToolBoxAiService.class.getName()));
                });

        @Test
        void test() {
            fail("Should not be called");
        }
    }

    @Nested
    class ToolsTest {
        @RegisterExtension
        static QuarkusUnitTest unitTest = new QuarkusUnitTest()
                .overrideConfigKey("quarkus.langchain4j.watsonx.base-url", WireMockUtil.URL_WATSONX_SERVER)
                .overrideConfigKey("quarkus.langchain4j.watsonx.iam.base-url", WireMockUtil.URL_IAM_SERVER)
                .overrideConfigKey("quarkus.langchain4j.watsonx.api-key", WireMockUtil.API_KEY)
                .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.project-id", WireMockUtil.PROJECT_ID)
                .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class).addClasses(ToolsAiService.class, MyTool.class))
                .assertException(t -> {
                    assertThat(t).isInstanceOf(RuntimeException.class)
                            .hasMessage(
                                    "You cannot use tools in conjunction with the use of @AgentLab. The content of the tools must be configured in the agent created in watsonx.ai agent lab UI. Offending class is %s"
                                            .formatted(ToolsAiService.class.getName()));
                });

        @Test
        void test() {
            fail("Should not be called");
        }
    }
}
