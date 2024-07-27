package io.quarkiverse.langchain4j.watsonx.prompt;

import static java.util.function.Predicate.not;

import java.util.List;
import java.util.stream.Stream;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageType;

/**
 * Defines the methods to format a {@link ChatMessage} into a prompt for watsonx.ai.
 */
public interface PromptFormatter {

    /**
     * The start <code>tag</code> of the prompt.
     */
    public default String start() {
        return "";
    }

    /**
     * The end <code>tag</code> of the prompt.
     */
    public default String end() {
        return "";
    }

    /**
     * Define how to join different messages.
     */
    public default String stringJoiner() {
        return "\n";
    }

    /**
     * The system <code>tag</code> of the prompt.
     */
    String system();

    /**
     * The user <code>tag</code> of the prompt.
     */
    String user();

    /**
     * The assistant <code>tag</code> of the prompt.
     */
    String assistant();

    /**
     * The tool <code>tag</code> of the prompt.
     */
    String tool();

    /**
     * Specify how to close a <code>tag</code> based on the type.
     */
    String endOf(ChatMessageType type);

    /**
     * Specify how to close a <code>tag</code> based on the message.
     */
    default String endOf(ChatMessage message) {
        return endOf(message.type());
    }

    /**
     * Return the prompt <code>tag</code> of the specified message.
     */
    default String tagOf(ChatMessage message) {
        return tagOf(message.type());
    }

    /**
     * Return the list of tokens.
     */
    default List<String> tokens() {
        return Stream.of(system(), user(), assistant(), tool())
                .map(String::trim)
                .filter(not(String::isBlank))
                .toList();
    }

    /**
     * Return the prompt <code>tag</code> of the specified type.
     */
    default String tagOf(ChatMessageType type) {
        return switch (type) {
            case AI -> assistant();
            case SYSTEM -> system();
            case TOOL_EXECUTION_RESULT -> tool();
            case USER -> user();
        };
    }
}
