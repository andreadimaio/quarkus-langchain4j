package io.quarkiverse.langchain4j.watsonx.runtime.client.impl;

import static io.quarkiverse.langchain4j.watsonx.runtime.client.WatsonxRestClientUtils.retryOn;

import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.jboss.resteasy.reactive.client.api.LoggingScope;

import com.ibm.watsonx.ai.textextraction.TextExtractionResponse;
import com.ibm.watsonx.ai.textextraction.TextExtractionRestClient;

import io.quarkiverse.langchain4j.watsonx.runtime.client.TextExtractionRestApi;
import io.quarkiverse.langchain4j.watsonx.runtime.client.WatsonxRestClientUtils;
import io.quarkiverse.langchain4j.watsonx.runtime.client.filter.BearerTokenHeaderFactory;
import io.quarkiverse.langchain4j.watsonx.runtime.client.logger.WatsonxClientLogger;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;

public final class QuarkusTextExtractionRestClient extends TextExtractionRestClient {

    private final TextExtractionRestApi textExtractionClient;
    private final TextExtractionRestApi cosClient;

    QuarkusTextExtractionRestClient(Builder builder) {
        super(builder);
        try {
            var textExtractionClientBuilder = QuarkusRestClientBuilder.newBuilder()
                    .clientHeadersFactory(new BearerTokenHeaderFactory(authenticationProvider))
                    .connectTimeout(timeout.toSeconds(), TimeUnit.SECONDS)
                    .readTimeout(timeout.toSeconds(), TimeUnit.SECONDS);

            if (logRequests || logResponses) {
                textExtractionClientBuilder.loggingScope(LoggingScope.REQUEST_RESPONSE);
                textExtractionClientBuilder.clientLogger(new WatsonxClientLogger(logRequests, logResponses));
            }

            textExtractionClient = textExtractionClientBuilder.baseUrl(URI.create(baseUrl).toURL())
                    .build(TextExtractionRestApi.class);

            cosClient = textExtractionClientBuilder.baseUrl(URI.create(cosUrl).toURL())
                    .property("buffer-response", "true")
                    .build(TextExtractionRestApi.class);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean deleteFile(DeleteFileRequest request) {
        return retryOn(request.requestTrackingId(), new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                cosClient.deleteFile(request.bucketName(), request.fileName(), request.requestTrackingId());
                return true;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> asyncDeleteFile(DeleteFileRequest request) {
        return cosClient.asyncDeleteFile(request.bucketName(), request.fileName(), request.requestTrackingId())
                .map(v -> true)
                .onFailure(WatsonxRestClientUtils::shouldRetry).retry().atMost(10)
                .subscribe().asCompletionStage();
    }

    @Override
    public String readFile(ReadFileRequest request) {
        return retryOn(request.requestTrackingId(), new Callable<String>() {
            @Override
            public String call() throws Exception {
                return cosClient.readFile(request.bucketName(), request.fileName(), request.requestTrackingId());
            }
        });
    }

    @Override
    public boolean upload(UploadRequest request) {
        return retryOn(request.requestTrackingId(), new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                cosClient.upload(request.bucketName(), request.fileName(), request.requestTrackingId(), request.is());
                return true;
            }
        });
    }

    @Override
    public boolean deleteExtraction(DeleteExtractionRequest request) {
        return retryOn(request.requestTrackingId(), new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                textExtractionClient.deleteExtraction(
                        request.extractionId(),
                        request.requestTrackingId(),
                        request.parameters().getTransactionId(),
                        request.parameters().getProjectId(),
                        request.parameters().getSpaceId(),
                        request.parameters().getHardDelete().orElse(null),
                        version);
                return true;
            }
        });
    }

    @Override
    public TextExtractionResponse fetchExtractionDetails(FetchExtractionDetailsRequest request) {
        return retryOn(request.requestTrackingId(), new Callable<TextExtractionResponse>() {
            @Override
            public TextExtractionResponse call() throws Exception {
                return textExtractionClient.fetchExtractionDetails(
                        request.extractionId(),
                        request.requestTrackingId(),
                        request.parameters().getTransactionId(),
                        request.parameters().getProjectId(),
                        request.parameters().getSpaceId(),
                        version);
            }
        });
    }

    @Override
    public TextExtractionResponse startExtraction(StartExtractionRequest request) {
        return retryOn(request.requestTrackingId(), new Callable<TextExtractionResponse>() {
            @Override
            public TextExtractionResponse call() throws Exception {
                return textExtractionClient.startExtraction(
                        request.requestTrackingId(),
                        request.transactionId(),
                        version,
                        request.textExtractionRequest());
            }
        });
    }

    public static final class QuarkusTextExtractionRestClientBuilderFactory implements TextExtractionRestClientBuilderFactory {
        @Override
        public Builder get() {
            return new QuarkusTextExtractionRestClient.Builder();
        }
    }

    static final class Builder extends TextExtractionRestClient.Builder {
        @Override
        public TextExtractionRestClient build() {
            return new QuarkusTextExtractionRestClient(this);
        }
    }
}
