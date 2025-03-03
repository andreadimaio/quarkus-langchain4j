package io.quarkiverse.langchain4j.watsonx.tools;

import static io.quarkiverse.langchain4j.watsonx.WatsonxUtils.retryOn;
import static io.quarkiverse.langchain4j.watsonx.bean.UtilityAgentToolsRequest.ToolName.WEATHER;
import static java.util.Objects.isNull;

import java.util.concurrent.Callable;

import dev.langchain4j.Experimental;
import io.quarkiverse.langchain4j.watsonx.bean.UtilityAgentToolsRequest;
import io.quarkiverse.langchain4j.watsonx.bean.UtilityAgentToolsRequest.WeatherInput;
import io.quarkiverse.langchain4j.watsonx.bean.UtilityAgentToolsResponse;
import io.quarkiverse.langchain4j.watsonx.client.UtilityAgentToolsRestApi;
import io.quarkiverse.langchain4j.watsonx.exception.BuiltinToolException;

/**
 * Built-in tool to find the weather of a city.
 */
@Experimental
public class WeatherTool {

    private static final String NO_CITY_FOUND = "Unable to find coordinates of the location:";
    private UtilityAgentToolsRestApi client;

    public WeatherTool(UtilityAgentToolsRestApi utilityAgentToolRestApi) {
        this.client = utilityAgentToolRestApi;
    }

    public String find(String name, String country)
            throws WeatherToolException, NoCityFoundException {

        if (isNull(name) || name.isBlank())
            throw new IllegalArgumentException("The field \"name\" cannot be null or empty");

        var request = new UtilityAgentToolsRequest(WEATHER, new WeatherInput(name, country));

        try {
            var response = retryOn(new Callable<UtilityAgentToolsResponse>() {
                @Override
                public UtilityAgentToolsResponse call() throws Exception {
                    return client.run(request);
                }
            });

            return response.output();

        } catch (BuiltinToolException exception) {
            if (isNull(exception.details()))
                throw new WeatherToolException(exception.getMessage(), exception);

            if (exception.details().startsWith(NO_CITY_FOUND))
                throw new NoCityFoundException(name, country, exception);

            throw new WeatherToolException(exception.getMessage(), exception);
        }
    }

    public static class WeatherToolException extends Exception {
        public WeatherToolException(String message, Throwable e) {
            super(message);
        }
    }

    public static class NoCityFoundException extends WeatherToolException {
        public NoCityFoundException(String city, String country, Throwable e) {
            super("%s %s%s".formatted(NO_CITY_FOUND, city, isNull(country) ? "" : "," + country), e);
        }
    }
}
