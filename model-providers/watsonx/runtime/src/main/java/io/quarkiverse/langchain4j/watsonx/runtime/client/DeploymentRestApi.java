package io.quarkiverse.langchain4j.watsonx.runtime.client;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.jboss.resteasy.reactive.RestStreamElementType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.watsonx.ai.chat.ChatResponse;
import com.ibm.watsonx.ai.chat.model.TextChatRequest;
import com.ibm.watsonx.ai.deployment.DeploymentResource;
import com.ibm.watsonx.ai.textgeneration.TextGenerationResponse;
import com.ibm.watsonx.ai.textgeneration.TextRequest;
import com.ibm.watsonx.ai.timeseries.ForecastRequest;
import com.ibm.watsonx.ai.timeseries.ForecastResponse;

import io.quarkiverse.langchain4j.QuarkusJsonCodecFactory;
import io.quarkus.rest.client.reactive.jackson.ClientObjectMapper;
import io.smallrye.mutiny.Multi;

@Path("")
public interface DeploymentRestApi extends WatsonxRestClient {

    @GET
    @Path("/ml/v4/deployments/{deployment_id}")
    @Produces(MediaType.APPLICATION_JSON)
    DeploymentResource findById(
            @PathParam("deployment_id") String deploymentId,
            @HeaderParam(TRANSACTION_ID_HEADER) String transactionId,
            @QueryParam("project_id") String project_id,
            @QueryParam("space_id") String space_id,
            @QueryParam("version") String version);

    @POST
    @Path("/ml/v1/deployments/{deployment_id}/text/generation")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    TextGenerationResponse generate(
            @PathParam("deployment_id") String deploymentId,
            @HeaderParam(TRANSACTION_ID_HEADER) String transactionId,
            @QueryParam("version") String version,
            TextRequest textRequest);

    @POST
    @Path("/ml/v1/deployments/{deployment_id}/text/generation_stream")
    @Consumes(MediaType.APPLICATION_JSON)
    @RestStreamElementType(MediaType.APPLICATION_JSON)
    Multi<String> generateStreaming(
            @PathParam("deployment_id") String deploymentId,
            @HeaderParam(TRANSACTION_ID_HEADER) String transactionId,
            @QueryParam("version") String version,
            TextRequest textRequest);

    @POST
    @Path("/ml/v1/deployments/{deployment_id}/text/chat")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    ChatResponse chat(
            @PathParam("deployment_id") String deploymentId,
            @HeaderParam(TRANSACTION_ID_HEADER) String transactionId,
            @QueryParam("version") String version,
            TextChatRequest textChatRequest);

    @POST
    @Path("/ml/v1/deployments/{deployment_id}/text/chat_stream")
    @Consumes(MediaType.APPLICATION_JSON)
    @RestStreamElementType(MediaType.APPLICATION_JSON)
    Multi<String> chatStreaming(
            @PathParam("deployment_id") String deploymentId,
            @HeaderParam(TRANSACTION_ID_HEADER) String transactionId,
            @QueryParam("version") String version,
            TextChatRequest textChatRequest);

    @POST
    @Path("/ml/v1/deployments/{deployment_id}/time_series/forecast")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    ForecastResponse forecast(
            @PathParam("deployment_id") String deploymentId,
            @HeaderParam(TRANSACTION_ID_HEADER) String transactionId,
            @QueryParam("version") String version,
            ForecastRequest forecastRequest);

    @ClientObjectMapper
    static ObjectMapper objectMapper(ObjectMapper defaultObjectMapper) {
        return QuarkusJsonCodecFactory.SnakeCaseObjectMapperHolder.MAPPER;
    }
}
