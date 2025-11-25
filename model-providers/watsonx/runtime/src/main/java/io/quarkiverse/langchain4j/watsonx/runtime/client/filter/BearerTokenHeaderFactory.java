package io.quarkiverse.langchain4j.watsonx.runtime.client.filter;

import java.util.function.Function;

import jakarta.ws.rs.core.MultivaluedMap;

import com.ibm.watsonx.ai.core.auth.AuthenticationProvider;

import io.quarkus.rest.client.reactive.ReactiveClientHeadersFactory;
import io.smallrye.mutiny.Uni;

/**
 * Add the bearer token to the watsonx.ai APIs.
 */
public class BearerTokenHeaderFactory extends ReactiveClientHeadersFactory {

    private final AuthenticationProvider authenticationProvider;

    public BearerTokenHeaderFactory(AuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    @Override
    public Uni<MultivaluedMap<String, String>> getHeaders(
            MultivaluedMap<String, String> incomingHeaders,
            MultivaluedMap<String, String> clientOutgoingHeaders) {

        return Uni.createFrom()
                .completionStage(authenticationProvider.asyncToken())
                .onItem()
                .transform(new Function<String, String>() {
                    @Override
                    public String apply(String token) {
                        return "Bearer %s".formatted(token);
                    }
                })
                .map(new Function<String, MultivaluedMap<String, String>>() {
                    @Override
                    public MultivaluedMap<String, String> apply(String token) {
                        clientOutgoingHeaders.add("Authorization", token);
                        return clientOutgoingHeaders;
                    }
                });
    }
}
