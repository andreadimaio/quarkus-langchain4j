package io.quarkiverse.langchain4j.watsonx.runtime.client;

import static java.util.Objects.nonNull;

import java.util.StringJoiner;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.jboss.resteasy.reactive.RestStreamElementType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.watsonx.ai.chat.ChatResponse;
import com.ibm.watsonx.ai.chat.model.TextChatRequest;
import com.ibm.watsonx.ai.core.HttpUtils;
import com.ibm.watsonx.ai.core.exeception.WatsonxException;
import com.ibm.watsonx.ai.core.exeception.model.WatsonxError;

import io.quarkiverse.langchain4j.QuarkusJsonCodecFactory;
import io.quarkus.rest.client.reactive.ClientExceptionMapper;
import io.quarkus.rest.client.reactive.jackson.ClientObjectMapper;
import io.smallrye.mutiny.Multi;

@Path("/ml/v1")
public interface ChatRestApi extends WatsonxRestClient {

    @POST
    @Path("text/chat")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    ChatResponse chat(
            @HeaderParam(TRANSACTION_ID_HEADER) String transactionId,
            @QueryParam("version") String version,
            TextChatRequest request);

    @POST
    @Path("text/chat_stream")
    @Consumes(MediaType.APPLICATION_JSON)
    @RestStreamElementType(MediaType.APPLICATION_JSON)
    Multi<String> chatStreaming(
            @HeaderParam(TRANSACTION_ID_HEADER) String transactionId,
            @QueryParam("version") String version,
            TextChatRequest request);

    @ClientObjectMapper
    static ObjectMapper objectMapper(ObjectMapper defaultObjectMapper) {
        return QuarkusJsonCodecFactory.SnakeCaseObjectMapperHolder.MAPPER;
    }

    @ClientExceptionMapper
    static WatsonxException toException(jakarta.ws.rs.core.Response response) {
        MediaType mediaType = response.getMediaType();
        String body = response.readEntity(String.class);

        if (body.isEmpty())
            return new WatsonxException("Status code: " + response.getStatus(), response.getStatus(), null);

        var error = HttpUtils.parseErrorBody(body, mediaType.toString());
        var joiner = new StringJoiner("\n");

        if (nonNull(error.errors()) && error.errors().size() > 0) {
            for (WatsonxError.Error errorDetail : error.errors())
                joiner.add("%s: %s".formatted(errorDetail.code(), errorDetail.message()));
        }

        return new WatsonxException(joiner.toString(), response.getStatus(), error);
    }

}
