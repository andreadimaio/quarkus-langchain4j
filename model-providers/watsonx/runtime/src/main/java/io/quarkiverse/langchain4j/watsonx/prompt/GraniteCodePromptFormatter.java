package io.quarkiverse.langchain4j.watsonx.prompt;

import dev.langchain4j.data.message.ChatMessageType;

/**
 * Granite code prompt formatter.
 */
public class GraniteCodePromptFormatter implements PromptFormatter {

    @Override
    public String system() {
        return "System:\n";
    }

    @Override
    public String user() {
        return "Question:\n";
    }

    @Override
    public String assistant() {
        return "Answer:\n";
    }

    @Override
    public String endOf(ChatMessageType messageType) {
        return "\n";
    }

    @Override
    public String tool() {
        return "";
    }
}
