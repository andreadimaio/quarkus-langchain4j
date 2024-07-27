package io.quarkiverse.langchain4j.watsonx.prompt;

import dev.langchain4j.data.message.ChatMessageType;

/*
 * Default prompt formatter.
 */
public class DefaultPromptFormatter implements PromptFormatter {

    @Override
    public String system() {
        return "";
    }

    @Override
    public String user() {
        return "";

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
    public String endOf(ChatMessageType messageType) {
        return "";
    }
}
