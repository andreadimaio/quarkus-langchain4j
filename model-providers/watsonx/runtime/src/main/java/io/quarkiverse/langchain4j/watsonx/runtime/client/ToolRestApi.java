package io.quarkiverse.langchain4j.watsonx.runtime.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.ibm.watsonx.ai.tool.ToolRequest;
import com.ibm.watsonx.ai.tool.ToolService.Resources;
import com.ibm.watsonx.ai.tool.UtilityTool;

@Path("/v1-beta/utility_agent_tools")
public interface ToolRestApi extends WatsonxRestClient {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Resources getAll(@HeaderParam(TRANSACTION_ID_HEADER) String transactionId);

    @GET
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    UtilityTool getByName(
            @HeaderParam(TRANSACTION_ID_HEADER) String transactionId,
            @PathParam("name") String name);

    @POST
    @Path("/run")
    @Produces(MediaType.APPLICATION_JSON)
    String run(
            @HeaderParam(TRANSACTION_ID_HEADER) String transactionId,
            ToolRequest request);
}
