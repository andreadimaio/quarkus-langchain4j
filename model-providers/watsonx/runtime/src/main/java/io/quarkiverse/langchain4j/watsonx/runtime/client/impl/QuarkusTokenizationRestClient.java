package io.quarkiverse.langchain4j.watsonx.runtime.client.impl;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.jboss.resteasy.reactive.client.api.LoggingScope;

import com.ibm.watsonx.ai.tokenization.TokenizationRequest;
import com.ibm.watsonx.ai.tokenization.TokenizationResponse;
import com.ibm.watsonx.ai.tokenization.TokenizationRestClient;

import io.quarkiverse.langchain4j.watsonx.runtime.client.TokenizationRestApi;
import io.quarkiverse.langchain4j.watsonx.runtime.client.WatsonxClientLogger;
import io.quarkiverse.langchain4j.watsonx.runtime.client.filter.BearerTokenHeaderFactory;
import io.quarkiverse.langchain4j.watsonx.runtime.client.filter.RequestIdHeaderFactory;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;

public final class QuarkusTokenizationRestClient extends TokenizationRestClient {

    private final TokenizationRestApi client;

    QuarkusTokenizationRestClient(Builder builder) {
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

            client = restClientBuilder.build(TokenizationRestApi.class);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TokenizationResponse tokenize(String transactionId, TokenizationRequest request) {
        return client.tokenize(transactionId, version, request);
    }

    @Override
    public CompletableFuture<TokenizationResponse> asyncTokenize(String transactionId, TokenizationRequest request) {
        return client.asyncTokenize(transactionId, version, request).subscribeAsCompletionStage();
    }

    public static final class QuarkusTokenizationRestClientBuilderFactory implements TokenizationRestClientBuilderFactory {
        @Override
        public Builder get() {
            return new QuarkusTokenizationRestClient.Builder();
        }
    }

    static final class Builder extends TokenizationRestClient.Builder {
        @Override
        public TokenizationRestClient build() {
            return new QuarkusTokenizationRestClient(this);
        }
    }
}
