package io.quarkiverse.langchain4j.watsonx.prompt;

import static dev.langchain4j.data.message.ChatMessageType.USER;

import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageType;

/**
 * Format a conversation into a prompt for LLM.
 */
public class QuarkusPromptFormatter {

    private final String joiner;
    private final PromptFormatter promptFormatter;
    private final boolean isDefault;
    private static final Pattern pattern = Pattern.compile("\n+$");

    public QuarkusPromptFormatter(PromptFormatter promptFormatter, String joiner) {
        this.promptFormatter = promptFormatter;
        this.isDefault = promptFormatter instanceof DefaultPromptFormatter;
        this.joiner = joiner;
    }

    /**
     * Apply the model's tag to all messages in a conversation.
     *
     * @param messages {@link List} of {@link ChatMessage}.
     * @return {@link String} with the model's tag applied.
     */
    public String format(List<ChatMessage> messages) {

        if (messages == null)
            throw new RuntimeException("No messages provided");

        if (messages.isEmpty())
            return "";

        StringJoiner joiner = new StringJoiner((isDefault ? this.joiner : promptFormatter.stringJoiner()),
                promptFormatter.start(), promptFormatter.end());

        ChatMessage firstMessage = messages.get(0);
        joiner.add(getFormattedText(firstMessage));

        for (int i = 1; i < messages.size(); i++) {
            var formattedMessage = getFormattedText(messages.get(i));
            joiner.add(formattedMessage);
        }

        int last = messages.size() - 1;
        if (messages.get(last).type() != ChatMessageType.AI && !promptFormatter.tagOf(ChatMessageType.AI).isBlank()) {
            joiner.add(promptFormatter.tagOf(ChatMessageType.AI));
        }

        return joiner.toString();
    }

    /*
     * Convert the ChatMessage to a string with the model's tags.
     */
    private String getFormattedText(ChatMessage message) {
        String text = message.text();
        Matcher matcher = pattern.matcher(text);
        if (!isDefault && matcher.find()) {
            text = matcher.replaceAll("");
        } else if (isDefault && message.type().equals(USER) && matcher.find()) {
            text = matcher.replaceAll("");
        }
        return "%s%s%s".formatted(promptFormatter.tagOf(message), text, promptFormatter.endOf(message));
    }

    public boolean instanceOf(Class<? extends PromptFormatter> clazz) {
        if (clazz == null)
            return false;
        return clazz.isInstance(this.promptFormatter);
    }
}
