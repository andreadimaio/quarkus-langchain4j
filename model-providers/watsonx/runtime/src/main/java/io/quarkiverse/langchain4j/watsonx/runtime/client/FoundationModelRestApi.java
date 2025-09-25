package io.quarkiverse.langchain4j.watsonx.runtime.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.watsonx.ai.foundationmodel.FoundationModel;
import com.ibm.watsonx.ai.foundationmodel.FoundationModelResponse;
import com.ibm.watsonx.ai.foundationmodel.FoundationModelTask;

import io.quarkiverse.langchain4j.QuarkusJsonCodecFactory;
import io.quarkus.rest.client.reactive.jackson.ClientObjectMapper;

@Path("/ml/v1")
public interface FoundationModelRestApi extends WatsonxRestClient {

    @GET
    @Path("foundation_model_specs")
    @Produces(MediaType.APPLICATION_JSON)
    FoundationModelResponse<FoundationModel> getModels(
            @QueryParam("start") Integer start,
            @QueryParam("limit") Integer limit,
            @HeaderParam(TRANSACTION_ID_HEADER) String transactionId,
            @QueryParam("tech_preview") Boolean techPreview,
            @QueryParam("version") String version,
            @QueryParam("filters") String filters);

    @GET
    @Path("foundation_model_tasks")
    @Produces(MediaType.APPLICATION_JSON)
    FoundationModelResponse<FoundationModelTask> getTasks(
            @QueryParam("start") Integer start,
            @QueryParam("limit") Integer limit,
            @HeaderParam(TRANSACTION_ID_HEADER) String transactionId,
            @QueryParam("version") String version);

    @ClientObjectMapper
    static ObjectMapper objectMapper(ObjectMapper defaultObjectMapper) {
        return QuarkusJsonCodecFactory.SnakeCaseObjectMapperHolder.MAPPER;
    }
}
