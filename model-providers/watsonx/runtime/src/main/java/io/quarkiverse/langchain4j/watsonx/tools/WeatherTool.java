package io.quarkiverse.langchain4j.watsonx.tools;

import static io.quarkiverse.langchain4j.watsonx.WatsonxUtils.retryOn;
import static io.quarkiverse.langchain4j.watsonx.bean.UtilityAgentToolsRequest.ToolName.WEATHER;

import java.util.Objects;
import java.util.concurrent.Callable;

import dev.langchain4j.Experimental;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import io.quarkiverse.langchain4j.watsonx.bean.UtilityAgentToolsRequest;
import io.quarkiverse.langchain4j.watsonx.bean.UtilityAgentToolsRequest.WeatherInput;
import io.quarkiverse.langchain4j.watsonx.bean.UtilityAgentToolsResponse;
import io.quarkiverse.langchain4j.watsonx.client.UtilityAgentToolsRestApi;
import io.quarkiverse.langchain4j.watsonx.exception.BuiltinToolException;

/**
 * Builtin tool to find the weather of a city.
 */
@Experimental
public class WeatherTool {

    private UtilityAgentToolsRestApi client;

    public WeatherTool(UtilityAgentToolsRestApi utilityAgentToolRestApi) {
        this.client = utilityAgentToolRestApi;
    }

    @Tool(value = "Find the weather for a city")
    public String find(
            @P(required = true, value = "Name of the location") String name,
            @P(required = false, value = "Name of the state or country") String country) throws BuiltinToolException {

        if (Objects.isNull(name) || name.isBlank())
            throw new IllegalArgumentException("The field \"name\" cannot be null or empty");

        var request = new UtilityAgentToolsRequest(WEATHER, new WeatherInput(name, country));
        var response = retryOn(new Callable<UtilityAgentToolsResponse>() {
            @Override
            public UtilityAgentToolsResponse call() throws Exception {
                return client.run(request);
            }
        });
        return response.output();
    }
}
