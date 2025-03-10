package io.quarkiverse.langchain4j.watsonx.runtime;

import static io.quarkiverse.langchain4j.runtime.OptionalUtil.firstOrDefault;
import static io.quarkiverse.langchain4j.watsonx.runtime.TokenGenerationCache.getOrCreateTokenGenerator;
import static java.util.Objects.isNull;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.jboss.resteasy.reactive.client.api.LoggingScope;

import io.quarkiverse.langchain4j.watsonx.client.UtilityAgentToolsRestApi;
import io.quarkiverse.langchain4j.watsonx.client.WatsonxClientLogger;
import io.quarkiverse.langchain4j.watsonx.client.filter.BearerTokenHeaderFactory;
import io.quarkiverse.langchain4j.watsonx.runtime.config.BuiltinToolConfig;
import io.quarkiverse.langchain4j.watsonx.runtime.config.IAMConfig;
import io.quarkiverse.langchain4j.watsonx.runtime.config.LangChain4jWatsonxConfig;
import io.quarkiverse.langchain4j.watsonx.tools.GoogleSearchTool;
import io.quarkiverse.langchain4j.watsonx.tools.WeatherTool;
import io.quarkiverse.langchain4j.watsonx.tools.WebCrawlerTool;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import io.quarkus.runtime.annotations.Recorder;
import io.smallrye.config.ConfigValidationException;

@Recorder
public class BuiltinToolRecorder {

    private static final ConfigValidationException.Problem[] EMPTY_PROBLEMS = new ConfigValidationException.Problem[0];
    private static final String MISSING_BUILTIN_TOOL_PROPERTY_ERROR = "To use the built-in tool classes, you must set the property 'quarkus.langchain4j.watsonx.built-in.%s'";
    private static final String INVALID_BASE_URL_ERROR = "The property 'quarkus.langchain4j.watsonx.base-url' does not have a correct url. Use one of the urls given in the documentation or use the property 'quarkus.langchain4j.watsonx.wx-base-url' to set a custom url for the agent-tool service";

    public Supplier<WebCrawlerTool> webCrawlerTool(LangChain4jWatsonxConfig runtimeConfig) {

        IAMConfig iamConfig = runtimeConfig.defaultConfig().iam();
        BuiltinToolConfig builtinToolConfig = runtimeConfig.builtinTool();

        String baseUrl = firstOrDefault(
                getWxBaseUrl(runtimeConfig.defaultConfig().baseUrl()),
                builtinToolConfig.baseUrl(),
                runtimeConfig.defaultConfig().wxBaseUrl());

        if (isNull(baseUrl) && runtimeConfig.defaultConfig().baseUrl().isPresent())
            throw new RuntimeException(INVALID_BASE_URL_ERROR);

        String apiKey = firstOrDefault(
                runtimeConfig.defaultConfig().apiKey().orElse(null),
                builtinToolConfig.apiKey());

        var configProblems = checkConfigurations(baseUrl, apiKey);
        if (!configProblems.isEmpty()) {
            throw new ConfigValidationException(configProblems.toArray(EMPTY_PROBLEMS));
        }

        Duration timeout = firstOrDefault(Duration.ofSeconds(10),
                builtinToolConfig.timeout(),
                runtimeConfig.defaultConfig().timeout());

        boolean logRequests = firstOrDefault(
                runtimeConfig.defaultConfig().logRequests().orElse(false),
                builtinToolConfig.logRequests());

        boolean logResponses = firstOrDefault(
                runtimeConfig.defaultConfig().logResponses().orElse(false),
                builtinToolConfig.logResponses());

        return new Supplier<WebCrawlerTool>() {
            @Override
            public WebCrawlerTool get() {
                return new WebCrawlerTool(
                        createRestClient(baseUrl, apiKey, iamConfig, timeout, logRequests, logResponses));
            }
        };
    }

    public Supplier<GoogleSearchTool> googleSearchTool(LangChain4jWatsonxConfig runtimeConfig) {

        IAMConfig iamConfig = runtimeConfig.defaultConfig().iam();
        BuiltinToolConfig builtinToolConfig = runtimeConfig.builtinTool();

        String baseUrl = firstOrDefault(
                getWxBaseUrl(runtimeConfig.defaultConfig().baseUrl()),
                builtinToolConfig.baseUrl(),
                runtimeConfig.defaultConfig().wxBaseUrl());

        if (isNull(baseUrl) && runtimeConfig.defaultConfig().baseUrl().isPresent())
            throw new RuntimeException(INVALID_BASE_URL_ERROR);

        String apiKey = firstOrDefault(
                runtimeConfig.defaultConfig().apiKey().orElse(null),
                builtinToolConfig.apiKey());

        var configProblems = checkConfigurations(baseUrl, apiKey);
        if (!configProblems.isEmpty()) {
            throw new ConfigValidationException(configProblems.toArray(EMPTY_PROBLEMS));
        }

        Duration timeout = firstOrDefault(Duration.ofSeconds(10),
                builtinToolConfig.timeout(),
                runtimeConfig.defaultConfig().timeout());

        boolean logRequests = firstOrDefault(
                runtimeConfig.defaultConfig().logRequests().orElse(false),
                builtinToolConfig.logRequests());

        boolean logResponses = firstOrDefault(
                runtimeConfig.defaultConfig().logResponses().orElse(false),
                builtinToolConfig.logResponses());

        return new Supplier<GoogleSearchTool>() {
            @Override
            public GoogleSearchTool get() {
                return new GoogleSearchTool(
                        createRestClient(baseUrl, apiKey, iamConfig, timeout, logRequests, logResponses));
            }
        };
    }

    public Supplier<WeatherTool> weatherTool(LangChain4jWatsonxConfig runtimeConfig) {

        IAMConfig iamConfig = runtimeConfig.defaultConfig().iam();
        BuiltinToolConfig builtinToolConfig = runtimeConfig.builtinTool();

        String baseUrl = firstOrDefault(
                getWxBaseUrl(runtimeConfig.defaultConfig().baseUrl()),
                builtinToolConfig.baseUrl(),
                runtimeConfig.defaultConfig().wxBaseUrl());

        if (isNull(baseUrl) && runtimeConfig.defaultConfig().baseUrl().isPresent())
            throw new RuntimeException(INVALID_BASE_URL_ERROR);

        String apiKey = firstOrDefault(
                runtimeConfig.defaultConfig().apiKey().orElse(null),
                builtinToolConfig.apiKey());

        var configProblems = checkConfigurations(baseUrl, apiKey);
        if (!configProblems.isEmpty()) {
            throw new ConfigValidationException(configProblems.toArray(EMPTY_PROBLEMS));
        }

        Duration timeout = firstOrDefault(Duration.ofSeconds(10),
                builtinToolConfig.timeout(),
                runtimeConfig.defaultConfig().timeout());

        boolean logRequests = firstOrDefault(
                runtimeConfig.defaultConfig().logRequests().orElse(false),
                builtinToolConfig.logRequests());

        boolean logResponses = firstOrDefault(
                runtimeConfig.defaultConfig().logResponses().orElse(false),
                builtinToolConfig.logResponses());

        return new Supplier<WeatherTool>() {
            @Override
            public WeatherTool get() {
                return new WeatherTool(
                        createRestClient(baseUrl, apiKey, iamConfig, timeout, logRequests, logResponses));
            }
        };
    }

    private UtilityAgentToolsRestApi createRestClient(
            String baseUrl, String apiKey, IAMConfig iamConfig,
            Duration timeout, boolean logRequests, boolean logResponses) {
        var builder = QuarkusRestClientBuilder.newBuilder()
                .baseUri(URI.create(baseUrl))
                .clientHeadersFactory(
                        new BearerTokenHeaderFactory(getOrCreateTokenGenerator(
                                apiKey,
                                iamConfig.baseUrl(),
                                iamConfig.grantType(),
                                iamConfig.timeout().orElse(Duration.ofSeconds(10)))))
                .readTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .connectTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);

        if (logRequests || logResponses) {
            builder.loggingScope(LoggingScope.REQUEST_RESPONSE)
                    .clientLogger(new WatsonxClientLogger(logRequests, logResponses));
        }

        return builder.build(UtilityAgentToolsRestApi.class);
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
                    .add(new ConfigValidationException.Problem(MISSING_BUILTIN_TOOL_PROPERTY_ERROR.formatted("base-url")));

        if (isNull(apiKey))
            configProblems.add(new ConfigValidationException.Problem(MISSING_BUILTIN_TOOL_PROPERTY_ERROR.formatted("api-key")));

        return configProblems;
    }
}
