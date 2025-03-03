package io.quarkiverse.langchain4j.watsonx.runtime;

import static io.quarkiverse.langchain4j.watsonx.runtime.TokenGenerationCache.getOrCreateTokenGenerator;

import java.net.URI;
import java.time.Duration;
import java.util.function.Supplier;

import org.jboss.resteasy.reactive.client.api.LoggingScope;

import io.quarkiverse.langchain4j.watsonx.client.UtilityAgentToolsRestApi;
import io.quarkiverse.langchain4j.watsonx.client.WatsonxClientLogger;
import io.quarkiverse.langchain4j.watsonx.client.filter.BearerTokenHeaderFactory;
import io.quarkiverse.langchain4j.watsonx.runtime.config.LangChain4jWatsonxConfig;
import io.quarkiverse.langchain4j.watsonx.tools.GoogleSearchTool;
import io.quarkiverse.langchain4j.watsonx.tools.WeatherTool;
import io.quarkiverse.langchain4j.watsonx.tools.WebCrawlerTool;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class BuiltinToolRecorder {

    private static final String BUILTIN_ERROR_MESSAGE = "To use the built-in tool classes, you must set the property 'quarkus.langchain4j.watsonx.%s'";

    public Supplier<WebCrawlerTool> webCrawlerTool(LangChain4jWatsonxConfig runtimeConfig) {

        var iamConfig = runtimeConfig.defaultConfig().iam();
        var builtinToolConfig = runtimeConfig.builtinTool();
        var apiKey = runtimeConfig.defaultConfig().apiKey()
                .orElseThrow(new Supplier<RuntimeException>() {
                    @Override
                    public RuntimeException get() {
                        return new RuntimeException(BUILTIN_ERROR_MESSAGE.formatted("api-key"));
                    }
                });

        return new Supplier<WebCrawlerTool>() {
            @Override
            public WebCrawlerTool get() {
                return new WebCrawlerTool(
                        QuarkusRestClientBuilder.newBuilder()
                                .baseUri(URI.create(getWxBaseUrl(runtimeConfig)))
                                .clientHeadersFactory(
                                        new BearerTokenHeaderFactory(getOrCreateTokenGenerator(
                                                apiKey,
                                                iamConfig.baseUrl(),
                                                iamConfig.grantType(),
                                                iamConfig.timeout().orElse(Duration.ofSeconds(10)))))
                                .loggingScope(LoggingScope.REQUEST_RESPONSE)
                                .clientLogger(new WatsonxClientLogger(builtinToolConfig.logRequests(),
                                        builtinToolConfig.logRequests()))
                                .build(UtilityAgentToolsRestApi.class));
            }
        };
    }

    public Supplier<GoogleSearchTool> googleSearchTool(LangChain4jWatsonxConfig runtimeConfig) {

        var iamConfig = runtimeConfig.defaultConfig().iam();
        var builtinToolConfig = runtimeConfig.builtinTool();
        var apiKey = runtimeConfig.defaultConfig().apiKey()
                .orElseThrow(new Supplier<RuntimeException>() {
                    @Override
                    public RuntimeException get() {
                        return new RuntimeException(BUILTIN_ERROR_MESSAGE.formatted("api-key"));
                    }
                });

        return new Supplier<GoogleSearchTool>() {
            @Override
            public GoogleSearchTool get() {
                return new GoogleSearchTool(
                        QuarkusRestClientBuilder.newBuilder()
                                .baseUri(URI.create(getWxBaseUrl(runtimeConfig)))
                                .clientHeadersFactory(
                                        new BearerTokenHeaderFactory(getOrCreateTokenGenerator(
                                                apiKey,
                                                iamConfig.baseUrl(),
                                                iamConfig.grantType(),
                                                iamConfig.timeout().orElse(Duration.ofSeconds(10)))))
                                .loggingScope(LoggingScope.REQUEST_RESPONSE)
                                .clientLogger(new WatsonxClientLogger(builtinToolConfig.logRequests(),
                                        builtinToolConfig.logRequests()))
                                .build(UtilityAgentToolsRestApi.class));
            }
        };
    }

    public Supplier<WeatherTool> weatherTool(LangChain4jWatsonxConfig runtimeConfig) {

        var iamConfig = runtimeConfig.defaultConfig().iam();
        var builtinToolConfig = runtimeConfig.builtinTool();
        var apiKey = runtimeConfig.defaultConfig().apiKey()
                .orElseThrow(new Supplier<RuntimeException>() {
                    @Override
                    public RuntimeException get() {
                        return new RuntimeException(BUILTIN_ERROR_MESSAGE.formatted("api-key"));
                    }
                });

        return new Supplier<WeatherTool>() {
            @Override
            public WeatherTool get() {
                return new WeatherTool(
                        QuarkusRestClientBuilder.newBuilder()
                                .baseUri(URI.create(getWxBaseUrl(runtimeConfig)))
                                .clientHeadersFactory(
                                        new BearerTokenHeaderFactory(getOrCreateTokenGenerator(
                                                apiKey,
                                                iamConfig.baseUrl(),
                                                iamConfig.grantType(),
                                                iamConfig.timeout().orElse(Duration.ofSeconds(10)))))
                                .loggingScope(LoggingScope.REQUEST_RESPONSE)
                                .clientLogger(new WatsonxClientLogger(builtinToolConfig.logRequests(),
                                        builtinToolConfig.logRequests()))
                                .build(UtilityAgentToolsRestApi.class));
            }
        };
    }

    private String getWxBaseUrl(LangChain4jWatsonxConfig runtimeConfig) {
        return runtimeConfig.defaultConfig().wxBaseUrl().orElseGet(new Supplier<String>() {
            @Override
            public String get() {
                String url = runtimeConfig.defaultConfig().baseUrl()
                        .orElseThrow(() -> new RuntimeException(BUILTIN_ERROR_MESSAGE.formatted("base-url")));
                return switch (url) {
                    case "https://us-south.ml.cloud.ibm.com" -> "https://api.dataplatform.cloud.ibm.com/wx";
                    case "https://eu-de.ml.cloud.ibm.com" -> "https://api.eu-de.dataplatform.cloud.ibm.com/wx";
                    case "https://eu-gb.ml.cloud.ibm.com" -> "https://api.eu-gb.dataplatform.cloud.ibm.com/wx";
                    case "https://jp-tok.ml.cloud.ibm.com" -> "https://api.jp-tok.dataplatform.cloud.ibm.com/wx";
                    case "https://au-syd.ml.cloud.ibm.com" -> "https://api.au-syd.dai.cloud.ibm.com/wx";
                    case "https://ca-tor.ml.cloud.ibm.com" -> "https://api.ca-tor.dai.cloud.ibm.com/wx";
                    default -> throw new RuntimeException(
                            "The property 'quarkus.langchain4j.watsonx.base-url' does not have a correct url. Use one of the urls given in the documentation or use the property 'quarkus.langchain4j.watsonx.wx-base-url' to set a custom url to use the built-in tools");
                };
            }
        });
    }
}
