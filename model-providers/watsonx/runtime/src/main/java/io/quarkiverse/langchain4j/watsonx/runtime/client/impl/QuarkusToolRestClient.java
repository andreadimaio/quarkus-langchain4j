package io.quarkiverse.langchain4j.watsonx.runtime.client.impl;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.jboss.resteasy.reactive.client.api.LoggingScope;

import com.ibm.watsonx.ai.tool.ToolRequest;
import com.ibm.watsonx.ai.tool.ToolRestClient;
import com.ibm.watsonx.ai.tool.ToolService.Resources;
import com.ibm.watsonx.ai.tool.UtilityTool;

import io.quarkiverse.langchain4j.watsonx.runtime.client.ToolRestApi;
import io.quarkiverse.langchain4j.watsonx.runtime.client.WatsonxClientLogger;
import io.quarkiverse.langchain4j.watsonx.runtime.client.filter.BearerTokenHeaderFactory;
import io.quarkiverse.langchain4j.watsonx.runtime.client.filter.RequestIdHeaderFactory;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;

public final class QuarkusToolRestClient extends ToolRestClient {

    private final ToolRestApi client;

    QuarkusToolRestClient(Builder builder) {
        super(builder);
        try {
            var restClientBuilder = QuarkusRestClientBuilder.newBuilder()
                    .baseUrl(URI.create(baseUrl).toURL())
                    .register(RequestIdHeaderFactory.class)
                    .clientHeadersFactory(new BearerTokenHeaderFactory(authenticationProvider))
                    .connectTimeout(timeout.toSeconds(), TimeUnit.SECONDS)
                    .readTimeout(timeout.toSeconds(), TimeUnit.SECONDS);

            if (logRequests || logResponses) {
                restClientBuilder.loggingScope(LoggingScope.REQUEST_RESPONSE);
                restClientBuilder.clientLogger(new WatsonxClientLogger(logRequests, logResponses));
            }

            client = restClientBuilder.build(ToolRestApi.class);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Resources getAll(String transactionId) {
        return client.getAll(transactionId);
    }

    @Override
    public UtilityTool getByName(String transactionId, String name) {
        return client.getByName(transactionId, name);
    }

    @Override
    public String run(String transactionId, ToolRequest request) {
        return client.run(transactionId, request);
    }

    public static final class QuarkusToolRestClientBuilderFactory implements ToolRestClientBuilderFactory {
        @Override
        public Builder get() {
            return new QuarkusToolRestClient.Builder();
        }
    }

    static final class Builder extends ToolRestClient.Builder {
        @Override
        public ToolRestClient build() {
            return new QuarkusToolRestClient(this);
        }
    }
}
