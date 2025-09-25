package io.quarkiverse.langchain4j.watsonx.runtime.client.filter;

import static java.util.Objects.isNull;

import java.io.IOException;
import java.util.UUID;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;

import com.ibm.watsonx.ai.core.http.BaseHttpClient;

/**
 * Filters the client request context generates a "Watsonx-AI-SDK-Request-Id" header if it is not present in the request.
 */
public class RequestIdHeaderFactory implements ClientRequestFilter {

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        String requestId = requestContext.getHeaderString(BaseHttpClient.REQUEST_ID_HEADER);
        if (isNull(requestId))
            requestContext.getHeaders().add(BaseHttpClient.REQUEST_ID_HEADER, UUID.randomUUID().toString());
    }
}
