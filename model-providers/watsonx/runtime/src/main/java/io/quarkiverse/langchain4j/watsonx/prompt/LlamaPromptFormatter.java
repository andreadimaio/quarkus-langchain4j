package io.quarkiverse.langchain4j.watsonx.prompt;

import dev.langchain4j.data.message.ChatMessageType;

/**
 * Llama prompt formatter.
 */
public class LlamaPromptFormatter implements PromptFormatter {

    @Override
    public String start() {
        return "<|begin_of_text|>";
    }

    @Override
    public String stringJoiner() {
        return "";
    }

    @Override
    public String system() {
        return "<|start_header_id|>system<|end_header_id|>\n\n";
    }

    @Override
    public String user() {
        return "<|start_header_id|>user<|end_header_id|>\n";
    }

    @Override
    public String assistant() {
        return "<|start_header_id|>assistant<|end_header_id|>\n";
    }

    @Override
    public String tool() {
        return "<|start_header_id|>ipython<|end_header_id|>\n";
    }

    @Override
    public String endOf(ChatMessageType type) {
        return "<|eot_id|>";
    }
}
