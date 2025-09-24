package io.quarkiverse.langchain4j.watsonx.runtime;

import static io.quarkiverse.langchain4j.runtime.OptionalUtil.firstOrDefault;
import static java.util.Objects.isNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.ibm.watsonx.ai.tool.builtin.GoogleSearchTool;
import com.ibm.watsonx.ai.tool.builtin.WeatherTool;
import com.ibm.watsonx.ai.tool.builtin.WebCrawlerTool;
import com.ibm.watsonx.ai.tool.builtin.WikipediaTool;

import io.quarkiverse.langchain4j.watsonx.runtime.config.BuiltinServiceConfig;
import io.quarkiverse.langchain4j.watsonx.runtime.config.IAMConfig;
import io.quarkiverse.langchain4j.watsonx.runtime.config.LangChain4jWatsonxConfig;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;
import io.smallrye.config.ConfigValidationException;

@Recorder
public class BuiltinToolRecorder {

    private static final ConfigValidationException.Problem[] EMPTY_PROBLEMS = new ConfigValidationException.Problem[0];
    private static final String MISSING_BUILTIN_SERVICE_PROPERTY_ERROR = "To use the built-in service classes, you must set the property 'quarkus.langchain4j.watsonx.built-in.%s'";
    private static final String INVALID_BASE_URL_ERROR = "The property 'quarkus.langchain4j.watsonx.base-url' does not have a correct url. Use one of the urls given in the documentation or use the property 'quarkus.langchain4j.watsonx.built-in-service.base-url' to set a custom url.";

    private final RuntimeValue<LangChain4jWatsonxConfig> runtimeConfig;

    public BuiltinToolRecorder(RuntimeValue<LangChain4jWatsonxConfig> runtimeConfig) {
        this.runtimeConfig = runtimeConfig;
    }

    public Supplier<WebCrawlerTool> webCrawler() {

        IAMConfig iamConfig = runtimeConfig.getValue().defaultConfig().iam();
        BuiltinServiceConfig builtinToolConfig = runtimeConfig.getValue().builtInService();

        String baseUrl = firstOrDefault(
                getWxBaseUrl(runtimeConfig.getValue().defaultConfig().baseUrl()),
                builtinToolConfig.baseUrl());

        if (isNull(baseUrl) && runtimeConfig.getValue().defaultConfig().baseUrl().isPresent())
            throw new RuntimeException(INVALID_BASE_URL_ERROR);

        String apiKey = firstOrDefault(
                runtimeConfig.getValue().defaultConfig().apiKey().orElse(null),
                builtinToolConfig.apiKey());

        var configProblems = checkConfigurations(baseUrl, apiKey);
        if (!configProblems.isEmpty()) {
            throw new ConfigValidationException(configProblems.toArray(EMPTY_PROBLEMS));
        }

        Duration timeout = firstOrDefault(
                runtimeConfig.getValue().defaultConfig().timeout().orElse(Duration.ofSeconds(10)),
                builtinToolConfig.timeout());

        boolean logRequests = firstOrDefault(
                runtimeConfig.getValue().defaultConfig().logRequests().orElse(false),
                builtinToolConfig.logRequests());

        boolean logResponses = firstOrDefault(
                runtimeConfig.getValue().defaultConfig().logResponses().orElse(false),
                builtinToolConfig.logResponses());

        return new Supplier<WebCrawlerTool>() {
            @Override
            public WebCrawlerTool get() {
                return new WebCrawlerTool(null);
            }
        };
    }

    public Supplier<GoogleSearchTool> googleSearch() {

        IAMConfig iamConfig = runtimeConfig.getValue().defaultConfig().iam();
        BuiltinServiceConfig builtinToolConfig = runtimeConfig.getValue().builtInService();

        String baseUrl = firstOrDefault(
                getWxBaseUrl(runtimeConfig.getValue().defaultConfig().baseUrl()),
                builtinToolConfig.baseUrl());

        if (isNull(baseUrl) && runtimeConfig.getValue().defaultConfig().baseUrl().isPresent())
            throw new RuntimeException(INVALID_BASE_URL_ERROR);

        String apiKey = firstOrDefault(
                runtimeConfig.getValue().defaultConfig().apiKey().orElse(null),
                builtinToolConfig.apiKey());

        var configProblems = checkConfigurations(baseUrl, apiKey);
        if (!configProblems.isEmpty()) {
            throw new ConfigValidationException(configProblems.toArray(EMPTY_PROBLEMS));
        }

        Duration timeout = firstOrDefault(
                runtimeConfig.getValue().defaultConfig().timeout().orElse(Duration.ofSeconds(10)),
                builtinToolConfig.timeout());

        boolean logRequests = firstOrDefault(
                runtimeConfig.getValue().defaultConfig().logRequests().orElse(false),
                builtinToolConfig.logRequests());

        boolean logResponses = firstOrDefault(
                runtimeConfig.getValue().defaultConfig().logResponses().orElse(false),
                builtinToolConfig.logResponses());

        return new Supplier<GoogleSearchTool>() {
            @Override
            public GoogleSearchTool get() {
                return new GoogleSearchTool(null);
            }
        };
    }

    public Supplier<WeatherTool> weather() {

        IAMConfig iamConfig = runtimeConfig.getValue().defaultConfig().iam();
        BuiltinServiceConfig builtinToolConfig = runtimeConfig.getValue().builtInService();

        String baseUrl = firstOrDefault(
                getWxBaseUrl(runtimeConfig.getValue().defaultConfig().baseUrl()),
                builtinToolConfig.baseUrl());

        if (isNull(baseUrl) && runtimeConfig.getValue().defaultConfig().baseUrl().isPresent())
            throw new RuntimeException(INVALID_BASE_URL_ERROR);

        String apiKey = firstOrDefault(
                runtimeConfig.getValue().defaultConfig().apiKey().orElse(null),
                builtinToolConfig.apiKey());

        var configProblems = checkConfigurations(baseUrl, apiKey);
        if (!configProblems.isEmpty()) {
            throw new ConfigValidationException(configProblems.toArray(EMPTY_PROBLEMS));
        }

        Duration timeout = firstOrDefault(
                runtimeConfig.getValue().defaultConfig().timeout().orElse(Duration.ofSeconds(10)),
                builtinToolConfig.timeout());

        boolean logRequests = firstOrDefault(
                runtimeConfig.getValue().defaultConfig().logRequests().orElse(false),
                builtinToolConfig.logRequests());

        boolean logResponses = firstOrDefault(
                runtimeConfig.getValue().defaultConfig().logResponses().orElse(false),
                builtinToolConfig.logResponses());

        return new Supplier<WeatherTool>() {
            @Override
            public WeatherTool get() {
                return new WeatherTool(null);
            }
        };
    }

    public Supplier<WikipediaTool> wikipedia() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'wikipedia'");
    }

    private String getWxBaseUrl(Optional<String> baseUrl) {
        if (baseUrl.isEmpty())
            return null;
        return switch (baseUrl.get()) {
            case "https://us-south.ml.cloud.ibm.com" -> "https://api.dataplatform.cloud.ibm.com/wx";
            case "https://eu-de.ml.cloud.ibm.com" -> "https://api.eu-de.dataplatform.cloud.ibm.com/wx";
            case "https://eu-gb.ml.cloud.ibm.com" -> "https://api.eu-gb.dataplatform.cloud.ibm.com/wx";
            case "https://jp-tok.ml.cloud.ibm.com" -> "https://api.jp-tok.dataplatform.cloud.ibm.com/wx";
            case "https://au-syd.ml.cloud.ibm.com" -> "https://api.au-syd.dai.cloud.ibm.com/wx";
            case "https://ca-tor.ml.cloud.ibm.com" -> "https://api.ca-tor.dai.cloud.ibm.com/wx";
            default -> null;
        };
    }

    private List<ConfigValidationException.Problem> checkConfigurations(String baseUrl, String apiKey) {
        List<ConfigValidationException.Problem> configProblems = new ArrayList<>();
        if (isNull(baseUrl))
            configProblems
                    .add(new ConfigValidationException.Problem(MISSING_BUILTIN_SERVICE_PROPERTY_ERROR.formatted("base-url")));

        if (isNull(apiKey))
            configProblems
                    .add(new ConfigValidationException.Problem(MISSING_BUILTIN_SERVICE_PROPERTY_ERROR.formatted("api-key")));

        return configProblems;
    }
}
