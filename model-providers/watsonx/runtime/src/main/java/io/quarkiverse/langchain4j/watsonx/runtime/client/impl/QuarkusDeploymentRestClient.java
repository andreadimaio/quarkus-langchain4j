package io.quarkiverse.langchain4j.watsonx.runtime.client.impl;

import static java.util.Objects.nonNull;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.jboss.resteasy.reactive.client.api.LoggingScope;

import com.ibm.watsonx.ai.chat.ChatResponse;
import com.ibm.watsonx.ai.chat.ChatSubscriber;
import com.ibm.watsonx.ai.deployment.DeploymentResource;
import com.ibm.watsonx.ai.deployment.DeploymentRestClient;
import com.ibm.watsonx.ai.deployment.FindByIdRequest;
import com.ibm.watsonx.ai.textgeneration.TextGenerationResponse;
import com.ibm.watsonx.ai.textgeneration.TextGenerationSubscriber;
import com.ibm.watsonx.ai.timeseries.ForecastResponse;

import io.quarkiverse.langchain4j.watsonx.runtime.client.DeploymentRestApi;
import io.quarkiverse.langchain4j.watsonx.runtime.client.WatsonxClientLogger;
import io.quarkiverse.langchain4j.watsonx.runtime.client.filter.BearerTokenHeaderFactory;
import io.quarkiverse.langchain4j.watsonx.runtime.client.filter.RequestIdHeaderFactory;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;

public final class QuarkusDeploymentRestClient extends DeploymentRestClient {

    private final DeploymentRestApi client;

    QuarkusDeploymentRestClient(Builder builder) {
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

            client = restClientBuilder.build(DeploymentRestApi.class);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DeploymentResource findById(FindByIdRequest parameters) {
        return client.findById(
                parameters.getDeploymentId(),
                parameters.getTransactionId(),
                parameters.getProjectId(),
                parameters.getSpaceId(),
                version);
    }

    @Override
    public TextGenerationResponse generate(GenerateRequest request) {
        return client.generate(request.deploymentId(), request.transactionId(), version, request.textRequest());
    }

    @Override
    public CompletableFuture<Void> generateStreaming(GenerateStreamingRequest request) {

        var textRequest = request.textRequest();
        var subscriber = TextGenerationSubscriber.createSubscriber(request.handler());

        return client.generateStreaming(request.deploymentId(), request.transactionId(), version, textRequest)
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

    @Override
    public ChatResponse chat(ChatRequest request) {
        return client.chat(
                request.deploymentId(),
                request.transactionId(),
                version,
                request.textChatRequest());
    }

    @Override
    public CompletableFuture<Void> chatStreaming(ChatStreamingRequest request) {

        var textChatRequest = request.textChatRequest();

        var subscriber = ChatSubscriber.createSubscriber(
                textChatRequest.getToolChoiceOption(),
                ChatSubscriber.toolHasParameters(textChatRequest.getTools()),
                request.extractionTags(), request.handler());

        return client.chatStreaming(request.deploymentId(), request.transactionId(), version, textChatRequest)
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

    @Override
    public ForecastResponse forecast(ForecastRequest request) {
        return client.forecast(
                request.deploymentId(),
                request.transactionId(),
                version,
                request.forecastRequest());
    }

    public static final class QuarkusDeploymentRestClientBuilderFactory implements DeploymentRestClientBuilderFactory {
        @Override
        public Builder get() {
            return new QuarkusDeploymentRestClient.Builder();
        }
    }

    static final class Builder extends DeploymentRestClient.Builder {
        @Override
        public DeploymentRestClient build() {
            return new QuarkusDeploymentRestClient(this);
        }
    }
}
