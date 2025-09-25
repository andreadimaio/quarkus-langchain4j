package io.quarkiverse.langchain4j.watsonx.runtime.client.impl;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.jboss.resteasy.reactive.client.api.LoggingScope;

import com.ibm.watsonx.ai.foundationmodel.FoundationModel;
import com.ibm.watsonx.ai.foundationmodel.FoundationModelParameters;
import com.ibm.watsonx.ai.foundationmodel.FoundationModelResponse;
import com.ibm.watsonx.ai.foundationmodel.FoundationModelRestClient;
import com.ibm.watsonx.ai.foundationmodel.FoundationModelTask;

import io.quarkiverse.langchain4j.watsonx.runtime.client.FoundationModelRestApi;
import io.quarkiverse.langchain4j.watsonx.runtime.client.WatsonxClientLogger;
import io.quarkiverse.langchain4j.watsonx.runtime.client.filter.RequestIdHeaderFactory;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;

public final class QuarkusFoundationModelRestClient extends FoundationModelRestClient {

    private final FoundationModelRestApi client;

    QuarkusFoundationModelRestClient(Builder builder) {
        super(builder);
        try {
            var restClientBuilder = QuarkusRestClientBuilder.newBuilder()
                    .baseUrl(URI.create(baseUrl).toURL())
                    .register(RequestIdHeaderFactory.class)
                    .connectTimeout(timeout.toSeconds(), TimeUnit.SECONDS)
                    .readTimeout(timeout.toSeconds(), TimeUnit.SECONDS);

            if (logRequests || logResponses) {
                restClientBuilder.loggingScope(LoggingScope.REQUEST_RESPONSE);
                restClientBuilder.clientLogger(new WatsonxClientLogger(logRequests, logResponses));
            }

            client = restClientBuilder.build(FoundationModelRestApi.class);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FoundationModelResponse<FoundationModel> getModels(
            Integer start, Integer limit,
            String transactionId, Boolean techPreview, String filters) {
        return client.getModels(start, limit, transactionId, techPreview, version, filters);
    }

    @Override
    public FoundationModelResponse<FoundationModelTask> getTasks(FoundationModelParameters parameters) {
        return client.getTasks(parameters.getStart(), parameters.getLimit(), parameters.getTransactionId(), version);
    }

    public static final class QuarkusFoundationModelRestClientBuilderFactory
            implements FoundationModelRestClientBuilderFactory {
        @Override
        public Builder get() {
            return new QuarkusFoundationModelRestClient.Builder();
        }
    }

    static final class Builder extends FoundationModelRestClient.Builder {
        @Override
        public FoundationModelRestClient build() {
            return new QuarkusFoundationModelRestClient(this);
        }
    }
}
