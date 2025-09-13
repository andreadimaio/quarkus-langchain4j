package io.quarkiverse.langchain4j.watsonx.runtime;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.ibm.watsonx.ai.core.auth.AuthenticationProvider;
import com.ibm.watsonx.ai.core.auth.iam.IAMAuthenticator;

// TODO: TO REMOVE
public class TokenGenerationCache {

    private static final Map<String, AuthenticationProvider> cache = new ConcurrentHashMap<>();

    public static Optional<AuthenticationProvider> get(String apiKey) {
        return Optional.ofNullable(cache.get(apiKey));
    }

    public static AuthenticationProvider getOrCreateTokenGenerator(URI url, String apiKey) {
        return cache.computeIfAbsent(apiKey,
                new Function<String, AuthenticationProvider>() {
                    @Override
                    public AuthenticationProvider apply(String apiKey) {
                        return IAMAuthenticator.builder()
                                .url(url)
                                .apiKey(apiKey)
                                .build();
                    }
                });
    }
}
