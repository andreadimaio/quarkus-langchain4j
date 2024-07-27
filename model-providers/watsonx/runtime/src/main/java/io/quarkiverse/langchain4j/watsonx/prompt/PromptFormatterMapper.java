package io.quarkiverse.langchain4j.watsonx.prompt;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Utility class to map the model names to the corresponding {@link PromptFormatter}.
 */
public class PromptFormatterMapper {

    static final Map<String, PromptFormatter> promptFormatters = new HashMap<>();
    static final DefaultPromptFormatter defaultPromptFormatter = new DefaultPromptFormatter();

    static {

        MistralPromptFormatter mistralPromptFormatter = new MistralPromptFormatter();
        promptFormatters.put("mistralai/mistral-large", mistralPromptFormatter);
        promptFormatters.put("mistralai/mixtral-8x7b-instruct-v01", mistralPromptFormatter);
        promptFormatters.put("sdaia/allam-1-13b-instruct", mistralPromptFormatter);

        LlamaPromptFormatter llamaPromptFormatter = new LlamaPromptFormatter();
        promptFormatters.put("meta-llama/llama-3-405b-instruct", llamaPromptFormatter);
        promptFormatters.put("meta-llama/llama-3-70b-instruct", llamaPromptFormatter);
        promptFormatters.put("meta-llama/llama-3-8b-instruct", llamaPromptFormatter);

        GranitePromptFormatter granitePromptFormatter = new GranitePromptFormatter();
        promptFormatters.put("ibm/granite-13b-chat-v2", granitePromptFormatter);
        promptFormatters.put("ibm/granite-13b-instruct-v2", granitePromptFormatter);
        promptFormatters.put("ibm/granite-7b-lab", granitePromptFormatter);

        GraniteCodePromptFormatter graniteCodePromptFormatter = new GraniteCodePromptFormatter();
        promptFormatters.put("ibm/granite-20b-code-instruct", graniteCodePromptFormatter);
        promptFormatters.put("ibm/granite-34b-code-instruct", graniteCodePromptFormatter);
        promptFormatters.put("ibm/granite-3b-code-instruct", graniteCodePromptFormatter);
        promptFormatters.put("ibm/granite-8b-code-instruct", graniteCodePromptFormatter);
    }

    /**
     * Returns the {@link PromptFormatter} for the given model name.
     *
     * @param model The model name.
     * @return The {@link PromptFormatter} for the given model name or the default implementation.
     */
    public static PromptFormatter get(String model) {
        return promptFormatters.getOrDefault(model, defaultPromptFormatter);
    }

    /**
     * Returns the default {@link PromptFormatter}.
     */
    public static PromptFormatter getDefault() {
        return defaultPromptFormatter;
    }

    /**
     * Returns a set of all supported model names.
     */
    public static Set<String> models() {
        return promptFormatters.keySet();
    }
}
