package io.quarkiverse.langchain4j.watsonx.runtime.client.impl;

import static com.ibm.watsonx.ai.chat.ChatSubscriber.createSubscriber;
import static com.ibm.watsonx.ai.chat.ChatSubscriber.toolHasParameters;
import static io.quarkiverse.langchain4j.watsonx.runtime.client.WatsonxRestClientUtils.retryOn;
import static java.util.Objects.nonNull;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.jboss.resteasy.reactive.client.api.LoggingScope;

import com.ibm.watsonx.ai.chat.ChatHandler;
import com.ibm.watsonx.ai.chat.ChatResponse;
import com.ibm.watsonx.ai.chat.ChatRestClient;
import com.ibm.watsonx.ai.chat.model.ExtractionTags;
import com.ibm.watsonx.ai.chat.model.TextChatRequest;

import io.quarkiverse.langchain4j.watsonx.runtime.client.ChatRestApi;
import io.quarkiverse.langchain4j.watsonx.runtime.client.WatsonxRestClientUtils;
import io.quarkiverse.langchain4j.watsonx.runtime.client.filter.BearerTokenHeaderFactory;
import io.quarkiverse.langchain4j.watsonx.runtime.client.logger.WatsonxClientLogger;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;

public final class QuarkusChatRestClient extends ChatRestClient {

    private final ChatRestApi client;

    QuarkusChatRestClient(Builder builder) {
        super(builder);
        try {
            var restClientBuilder = QuarkusRestClientBuilder.newBuilder()
                    .baseUrl(URI.create(baseUrl).toURL())
                    .clientHeadersFactory(new BearerTokenHeaderFactory(authenticationProvider))
                    .connectTimeout(timeout.toSeconds(), TimeUnit.SECONDS)
                    .readTimeout(timeout.toSeconds(), TimeUnit.SECONDS);

            if (logRequests || logResponses) {
                restClientBuilder.loggingScope(LoggingScope.REQUEST_RESPONSE);
                restClientBuilder.clientLogger(new WatsonxClientLogger(logRequests, logResponses));
            }

            client = restClientBuilder.build(ChatRestApi.class);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ChatResponse chat(String transactionId, TextChatRequest textChatRequest) {
        var requestId = UUID.randomUUID().toString();
        return retryOn(requestId, new Callable<ChatResponse>() {
            @Override
            public ChatResponse call() throws Exception {
                return client.chat(UUID.randomUUID().toString(), transactionId, version, textChatRequest);
            }
        });
    }

    @Override
    public CompletableFuture<Void> chatStreaming(
            String transactionId,
            ExtractionTags extractionTags,
            TextChatRequest textChatRequest,
            ChatHandler handler) {

        var requestId = UUID.randomUUID().toString();

        var subscriber = createSubscriber(
                textChatRequest.toolChoiceOption(),
                toolHasParameters(textChatRequest.tools()),
                extractionTags, handler);

        return client.chatStreaming(requestId, transactionId, version, textChatRequest)
                .onItem().invoke(new Consumer<String>() {
                    @Override
                    public void accept(String message) {
                        if (nonNull(message) && !message.isBlank()) {
                            subscriber.onNext("data: " + message);
                        }
                    }
                })
                .onFailure(WatsonxRestClientUtils::shouldRetry).retry().atMost(10)
                .onFailure().invoke(subscriber::onError)
                .onCompletion().invoke(subscriber::onComplete)
                .collect().asList().replaceWithVoid()
                .subscribeAsCompletionStage();
    }

    public static final class QuarkusChatRestClientBuilderFactory implements ChatRestClientBuilderFactory {
        @Override
        public Builder get() {
            return new QuarkusChatRestClient.Builder();
        }
    }

    static final class Builder extends ChatRestClient.Builder {
        @Override
        public ChatRestClient build() {
            return new QuarkusChatRestClient(this);
        }
    }
}
