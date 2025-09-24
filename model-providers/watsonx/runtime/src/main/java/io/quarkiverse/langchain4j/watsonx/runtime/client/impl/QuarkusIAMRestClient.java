package io.quarkiverse.langchain4j.watsonx.runtime.client.impl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import com.ibm.watsonx.ai.core.auth.IdentityTokenResponse;
import com.ibm.watsonx.ai.core.auth.iam.IAMRestClient;
import io.quarkiverse.langchain4j.watsonx.runtime.client.IAMRestApi;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;

public final class QuarkusIAMRestClient extends IAMRestClient {

    private final IAMRestApi client;

    QuarkusIAMRestClient(Builder builder) {
        super(builder);
        try {
            client = QuarkusRestClientBuilder.newBuilder()
                    .baseUrl(baseUrl.toURL())
                    .connectTimeout(timeout.toSeconds(), TimeUnit.SECONDS)
                    .readTimeout(timeout.toSeconds(), TimeUnit.SECONDS)
                    .build(IAMRestApi.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public IdentityTokenResponse token(String apiKey, String grantType) {
        return client.token(apiKey, grantType).await().indefinitely();
    }

    @Override
    public CompletableFuture<IdentityTokenResponse> asyncToken(String apiKey, String grantType) {
        return client.token(apiKey, grantType).subscribeAsCompletionStage();
    }

    public static final class QuarkusIAMRestClientBuilderFactory implements IAMRestClientBuilderFactory {
        @Override
        public Builder get() {
            return new QuarkusIAMRestClient.Builder();
        }
    }

    static final class Builder extends IAMRestClient.Builder<QuarkusIAMRestClient, Builder> {
        @Override
        public QuarkusIAMRestClient build() {
            return new QuarkusIAMRestClient(this);
        }
    }
}
