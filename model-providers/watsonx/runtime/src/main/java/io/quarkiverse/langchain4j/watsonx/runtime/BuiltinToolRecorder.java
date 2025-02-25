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
import io.quarkiverse.langchain4j.watsonx.runtime.config.LangChain4jWatsonxFixedRuntimeConfig;
import io.quarkiverse.langchain4j.watsonx.tools.GoogleSearchTool;
import io.quarkiverse.langchain4j.watsonx.tools.WeatherTool;
import io.quarkiverse.langchain4j.watsonx.tools.WebCrawlerTool;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class BuiltinToolRecorder {

    public Supplier<WebCrawlerTool> webCrawlerTool(
            String wxBaseUrl,
            LangChain4jWatsonxConfig runtimeConfig,
            LangChain4jWatsonxFixedRuntimeConfig fixedRuntimeConfig) {

        var apiKey = fixedRuntimeConfig.defaultConfig().apiKey().orElseThrow();
        var iamConfig = fixedRuntimeConfig.defaultConfig().iam();
        var builtinToolConfig = runtimeConfig.builtinTool();

        return new Supplier<>() {
            @Override
            public WebCrawlerTool get() {
                return new WebCrawlerTool(
                        QuarkusRestClientBuilder.newBuilder()
                                .baseUri(URI.create(wxBaseUrl))
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

    public Supplier<GoogleSearchTool> googleSearchTool(
            String wxBaseUrl,
            LangChain4jWatsonxConfig runtimeConfig,
            LangChain4jWatsonxFixedRuntimeConfig fixedRuntimeConfig) {

        var apiKey = fixedRuntimeConfig.defaultConfig().apiKey().orElseThrow();
        var iamConfig = fixedRuntimeConfig.defaultConfig().iam();
        var builtinToolConfig = runtimeConfig.builtinTool();

        return new Supplier<>() {
            @Override
            public GoogleSearchTool get() {
                return new GoogleSearchTool(
                        QuarkusRestClientBuilder.newBuilder()
                                .baseUri(URI.create(wxBaseUrl))
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

    public Supplier<WeatherTool> weatherTool(
            String wxBaseUrl,
            LangChain4jWatsonxConfig runtimeConfig,
            LangChain4jWatsonxFixedRuntimeConfig fixedRuntimeConfig) {

        var apiKey = fixedRuntimeConfig.defaultConfig().apiKey().orElseThrow();
        var iamConfig = fixedRuntimeConfig.defaultConfig().iam();
        var builtinToolConfig = runtimeConfig.builtinTool();

        return new Supplier<>() {
            @Override
            public WeatherTool get() {
                return new WeatherTool(
                        QuarkusRestClientBuilder.newBuilder()
                                .baseUri(URI.create(wxBaseUrl))
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
}
