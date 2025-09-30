package io.quarkiverse.langchain4j.watsonx.runtime;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.ibm.watsonx.ai.core.auth.AuthenticationProvider;
import com.ibm.watsonx.ai.core.auth.iam.IAMAuthenticator;

public class AuthenticationProviderCache {

    private static final Map<String, AuthenticationProvider> cache = new ConcurrentHashMap<>();

    private AuthenticationProviderCache() {
    }

    static AuthenticationProvider getOrCreateTokenGenerator(URI baseUrl, String apiKey) {
        return cache.computeIfAbsent(apiKey,
                new Function<String, AuthenticationProvider>() {
                    @Override
                    public AuthenticationProvider apply(String apiKey) {
                        return IAMAuthenticator.builder()
                                .baseUrl(baseUrl)
                                .apiKey(apiKey)
                                .build();
                    }
                });
    }
}
