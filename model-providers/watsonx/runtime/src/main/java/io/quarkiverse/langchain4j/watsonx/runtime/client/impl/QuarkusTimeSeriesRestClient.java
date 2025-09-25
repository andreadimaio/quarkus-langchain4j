package io.quarkiverse.langchain4j.watsonx.runtime.client.impl;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.jboss.resteasy.reactive.client.api.LoggingScope;

import com.ibm.watsonx.ai.timeseries.ForecastRequest;
import com.ibm.watsonx.ai.timeseries.ForecastResponse;
import com.ibm.watsonx.ai.timeseries.TimeSeriesRestClient;

import io.quarkiverse.langchain4j.watsonx.runtime.client.TimeSeriesRestApi;
import io.quarkiverse.langchain4j.watsonx.runtime.client.filter.BearerTokenHeaderFactory;
import io.quarkiverse.langchain4j.watsonx.runtime.client.filter.RequestIdHeaderFactory;
import io.quarkiverse.langchain4j.watsonx.runtime.client.logger.WatsonxClientLogger;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;

public final class QuarkusTimeSeriesRestClient extends TimeSeriesRestClient {

    private final TimeSeriesRestApi client;

    QuarkusTimeSeriesRestClient(Builder builder) {
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

            client = restClientBuilder.build(TimeSeriesRestApi.class);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ForecastResponse forecast(String transactionId, ForecastRequest request) {
        return client.forecast(transactionId, version, request);
    }

    public static final class QuarkusTimeSeriesRestClientBuilderFactory implements TimeSeriesRestClientBuilderFactory {
        @Override
        public Builder get() {
            return new QuarkusTimeSeriesRestClient.Builder();
        }
    }

    static final class Builder extends TimeSeriesRestClient.Builder {
        @Override
        public TimeSeriesRestClient build() {
            return new QuarkusTimeSeriesRestClient(this);
        }
    }
}
