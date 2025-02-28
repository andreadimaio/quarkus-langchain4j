package io.quarkiverse.langchain4j.watsonx;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import dev.langchain4j.Experimental;
import io.quarkiverse.langchain4j.RegisterAiService;

/**
 * This annotation is used at the class level on an {@link AiService} annotated
 * with {@link RegisterAiService} to indicate that all methods within the interface
 * invoke a deployed Agent on watsonx.ai.
 *
 * <p>
 * Classes annotated with {@code @AgentLab} will automatically route all method
 * invocations to the configured agent within the watsonx.ai platform. The agent's
 * configuration—including the selection of the large language model, external tools,
 * and other parameters—is fully managed within the Agent Lab platform. As a result,
 * any local configuration properties will be ignored.
 * </p>
 *
 * <h3>Usage Requirements</h3>
 * <ul>
 * <li>{@code @AgentLab} must be used at the class level.</li>
 * <li>The {@code @AgentLab} annotation must specify a valid, non-empty deployment id.</li>
 * <li>The {@code @SystemMessage} cannot be used in conjunction with {@code @AgentLab}.</li>
 * <li>Tools (e.g., {@code ToolBox}) cannot be defined.</li>
 * <li>The agent must be used in {@code chat } mode, {@code generation } mode is not supported.</li>
 * </ul>
 *
 * <h3>Example Usage</h3>
 *
 * <pre>
 * {@code @RegisterAiService }
 * {@code @AgentLab("7e354c85-d195-4dca-b53e-9566ae9f535b") }
 * public interface MyAgent {
 *     String invoke(@UserMessage String message);
 *     {@code Multi<String> }invoke(@UserMessage String message);
 * }
 * </pre>
 */
@Retention(RUNTIME)
@Target(TYPE)
@Experimental
public @interface AgentLab {

    /**
     * The deployment id of the Agent.
     */
    String value();
}
