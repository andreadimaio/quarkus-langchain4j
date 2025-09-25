package io.quarkiverse.langchain4j.watsonx.runtime.client.impl;

import static java.util.Objects.nonNull;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.jboss.resteasy.reactive.client.api.LoggingScope;

import com.ibm.watsonx.ai.textgeneration.TextGenerationHandler;
import com.ibm.watsonx.ai.textgeneration.TextGenerationResponse;
import com.ibm.watsonx.ai.textgeneration.TextGenerationRestClient;
import com.ibm.watsonx.ai.textgeneration.TextGenerationSubscriber;
import com.ibm.watsonx.ai.textgeneration.TextRequest;

import io.quarkiverse.langchain4j.watsonx.runtime.client.TextGenerationRestApi;
import io.quarkiverse.langchain4j.watsonx.runtime.client.WatsonxClientLogger;
import io.quarkiverse.langchain4j.watsonx.runtime.client.filter.BearerTokenHeaderFactory;
import io.quarkiverse.langchain4j.watsonx.runtime.client.filter.RequestIdHeaderFactory;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;

public final class QuarkusTextGenerationRestClient extends TextGenerationRestClient {

    private final TextGenerationRestApi client;

    QuarkusTextGenerationRestClient(Builder builder) {
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

            client = restClientBuilder.build(TextGenerationRestApi.class);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TextGenerationResponse generate(String transactionId, TextRequest textRequest) {
        return client.generate(transactionId, version, textRequest);
    }

    @Override
    public CompletableFuture<Void> generateStreaming(String transactionId, TextRequest textRequest,
            TextGenerationHandler handler) {
        var subscriber = TextGenerationSubscriber.createSubscriber(handler);
        return client.generateStreaming(transactionId, version, textRequest)
                .onItem().invoke(new Consumer<String>() {
                    @Override
                    public void accept(String message) {
                        if (nonNull(message) && !message.isBlank()) {
                            subscriber.onNext("data: " + message);
                        }
                    }
                })
                .onFailure().invoke(subscriber::onError)
                .onCompletion().invoke(subscriber::onComplete)
                .collect().asList().replaceWithVoid()
                .subscribe().asCompletionStage();
    }

    public static final class QuarkusTextGenerationRestClientBuilderFactory implements TextGenerationRestClientBuilderFactory {
        @Override
        public Builder get() {
            return new QuarkusTextGenerationRestClient.Builder();
        }
    }

    static final class Builder extends TextGenerationRestClient.Builder {
        @Override
        public TextGenerationRestClient build() {
            return new QuarkusTextGenerationRestClient(this);
        }
    }
}
