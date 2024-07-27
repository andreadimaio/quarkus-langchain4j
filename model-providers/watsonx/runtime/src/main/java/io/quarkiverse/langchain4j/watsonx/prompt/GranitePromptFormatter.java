package io.quarkiverse.langchain4j.watsonx.prompt;

import dev.langchain4j.data.message.ChatMessageType;

/**
 * Granite prompt formatter.
 */
public class GranitePromptFormatter implements PromptFormatter {

    @Override
    public String system() {
        return "<|system|>\n";
    }

    @Override
    public String user() {
        return "<|user|>\n";
    }

    @Override
    public String assistant() {
        return "<|assistant|>\n";
    }

    @Override
    public String endOf(ChatMessageType messageType) {
        return "";
    }

    @Override
    public String tool() {
        return "";
    }
}
