package com.ibm.langchain4j.watsonx.deployment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import jakarta.inject.Singleton;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import dev.langchain4j.model.input.structured.StructuredPrompt;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkus.test.QuarkusUnitTest;

public class PromptFormatterExceptionTest {

    static final String MESSAGE_1 = "The \"chat-model.prompt-formatter\" property cannot be set to \"true\" if you are generating the prompt dinamically. Found in class %s";
    static final String MESSAGE_2 = "The prompt in the AIService \"%s\" already contains one or more tokens for the model \"%s\". To continue, remove the tokens or set the variable of the property \"chat-model.prompt-formatter\" to \"false\"";
    static final String MESSAGE_3 = "If the \"chat-model.prompt-formatter\" property is set to \"true\", all @RegisterAiService annotated classes assigned to the %s provider with the model name \"%s\" must contain at least one @UserMessage. Found in class %s";

    @StructuredPrompt("Create a poem about {topic}")
    static class PoemPrompt {

        private final String topic;

        public PoemPrompt(String topic) {
            this.topic = topic;
        }

        public String getTopic() {
            return topic;
        }
    }

    @RegisterAiService
    @Singleton
    interface AIService {
        @SystemMessage("This is a systemMessage")
        String chat(@UserMessage String prompt, @V("text") String text);
    }

    @RegisterAiService
    @Singleton
    interface AIServiceWithTokenInSystemMessage {
        @SystemMessage("<|system|>This is a systemMessage")
        @UserMessage("{text}")
        String chat(String text);
    }

    @RegisterAiService
    @Singleton
    interface AIServiceWithTokenInUserMessage {
        @SystemMessage("This is a systemMessage")
        @UserMessage("<|system|>{text}")
        String chat(String text);
    }

    @RegisterAiService
    @Singleton
    interface StructuredPromptAIService {
        String poem(PoemPrompt prompt);
    }

    @Nested
    class RuntimeLanguageModelTest {
        @RegisterExtension
        static QuarkusUnitTest unitTest = new QuarkusUnitTest()
                .overrideConfigKey("quarkus.langchain4j.watsonx.chat-model.prompt-formatter", "true")
                .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class).addClass(AIService.class))
                .assertException(t -> {
                    assertThat(t).isInstanceOf(RuntimeException.class)
                            .hasMessage(MESSAGE_1.formatted(AIService.class.getName()));
                });

        @Test
        void test() {
            fail("Should not be called");
        }
    }

    @Nested
    class LanguageModelWithTokenInSystemMessageTest {
        @RegisterExtension
        static QuarkusUnitTest unitTest = new QuarkusUnitTest()
                .overrideConfigKey("quarkus.langchain4j.watsonx.chat-model.prompt-formatter", "true")
                .setArchiveProducer(
                        () -> ShrinkWrap.create(JavaArchive.class).addClass(AIServiceWithTokenInSystemMessage.class))
                .assertException(t -> {
                    assertThat(t).isInstanceOf(RuntimeException.class)
                            .hasMessage(
                                    MESSAGE_2.formatted(AIServiceWithTokenInSystemMessage.class.getName(),
                                            WireMockUtil.DEFAULT_CHAT_MODEL));
                });

        @Test
        void test() {
            fail("Should not be called");
        }
    }

    @Nested
    class LanguageModelWithTokenInUserMessageTest {
        @RegisterExtension
        static QuarkusUnitTest unitTest = new QuarkusUnitTest()
                .overrideConfigKey("quarkus.langchain4j.watsonx.chat-model.prompt-formatter", "true")
                .setArchiveProducer(
                        () -> ShrinkWrap.create(JavaArchive.class).addClass(AIServiceWithTokenInUserMessage.class))
                .assertException(t -> {
                    assertThat(t).isInstanceOf(RuntimeException.class)
                            .hasMessage(
                                    MESSAGE_2.formatted(AIServiceWithTokenInUserMessage.class.getName(),
                                            WireMockUtil.DEFAULT_CHAT_MODEL));
                });

        @Test
        void test() {
            fail("Should not be called");
        }
    }

    @Nested
    class NoAnnotatedAiService {
        @RegisterExtension
        static QuarkusUnitTest unitTest = new QuarkusUnitTest()
                .overrideConfigKey("quarkus.langchain4j.watsonx.chat-model.prompt-formatter", "true")
                .setArchiveProducer(
                        () -> ShrinkWrap.create(JavaArchive.class).addClasses(AIService.class, StructuredPromptAIService.class,
                                PoemPrompt.class))
                .assertException(t -> {
                    assertThat(t).isInstanceOf(RuntimeException.class)
                            .hasMessage(
                                    MESSAGE_3.formatted("watsonx", "<default>", StructuredPromptAIService.class.getName()));
                });

        @Test
        void test() {
            fail("Should not be called");
        }
    }
}
