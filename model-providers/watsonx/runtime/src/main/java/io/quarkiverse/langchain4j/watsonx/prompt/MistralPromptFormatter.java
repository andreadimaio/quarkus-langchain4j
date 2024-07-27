package io.quarkiverse.langchain4j.watsonx.prompt;

import dev.langchain4j.data.message.ChatMessageType;

/**
 * Mistral prompt formatter.
 */
public class MistralPromptFormatter implements PromptFormatter {

    @Override
    public String start() {
        return "<s>";
    }

    @Override
    public String stringJoiner() {
        return "";
    }

    @Override
    public String system() {
        return "[INST] ";
    }

    @Override
    public String user() {
        return "[INST] ";
    }

    @Override
    public String assistant() {
        return "";
    }

    @Override
    public String tool() {
        return "";
    }

    @Override
    public String endOf(ChatMessageType type) {
        return switch (type) {
            case AI -> "</s> ";
            case SYSTEM -> " [/INST]</s> ";
            case USER -> " [/INST] ";
            case TOOL_EXECUTION_RESULT -> "";
        };
    }
}
