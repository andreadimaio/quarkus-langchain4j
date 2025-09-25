package io.quarkiverse.langchain4j.watsonx.runtime.client;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

import jakarta.ws.rs.WebApplicationException;

import com.ibm.watsonx.ai.core.exeception.WatsonxException;
import com.ibm.watsonx.ai.core.http.BaseHttpClient;
import com.ibm.watsonx.ai.core.http.interceptors.RetryInterceptor;

import io.quarkus.logging.Log;

public interface WatsonxRestClient {

    final String TRANSACTION_ID_HEADER = com.ibm.watsonx.ai.WatsonxRestClient.TRANSACTION_ID_HEADER;
    final String REQUEST_ID_HEADER = BaseHttpClient.REQUEST_ID_HEADER;

    final Predicate<Throwable> TOKEN_EXPIRED = RetryInterceptor.ON_TOKEN_EXPIRED.retryOn().get(0).predicate().orElseThrow();
    final Predicate<Throwable> ON_RETRYABLE_STATUS_CODES = RetryInterceptor.ON_RETRYABLE_STATUS_CODES.retryOn().get(0)
            .predicate().orElseThrow();

    static boolean shouldRetry(Throwable e) {
        return TOKEN_EXPIRED.test(e) || ON_RETRYABLE_STATUS_CODES.test(e);
    }

    static <T> T retryOn(String requestId, Callable<T> action) {
        int maxRetries = 10;
        var timeout = Duration.ofMillis(20);

        for (int i = 1; i <= maxRetries; i++) {

            try {

                return action.call();

            } catch (WatsonxException e) {

                if (shouldRetry(e)) {
                    if (i > 1) {
                        try {

                            Log.debugf(
                                    "Retrying request \"{}\" ({}/{}) after failure: {}",
                                    requestId, i, maxRetries, e.getMessage());

                            Thread.sleep(timeout.toMillis());

                        } catch (Exception ex) {
                            throw new RuntimeException(e);
                        }
                    }
                    timeout = timeout.multipliedBy(2);
                    continue;
                }

            } catch (WebApplicationException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("Request failed after " + maxRetries + " attempts");
    }

}
