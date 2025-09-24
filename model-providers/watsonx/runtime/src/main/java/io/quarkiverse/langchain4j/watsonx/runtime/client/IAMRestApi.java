package io.quarkiverse.langchain4j.watsonx.runtime.client;

import org.jboss.resteasy.reactive.RestForm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.watsonx.ai.core.auth.IdentityTokenResponse;
import io.quarkiverse.langchain4j.QuarkusJsonCodecFactory;
import io.quarkus.rest.client.reactive.jackson.ClientObjectMapper;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("")
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
@Produces(MediaType.APPLICATION_JSON)
public interface IAMRestApi {

    @POST
    @Path("/identity/token")
    Uni<IdentityTokenResponse> token(
            @RestForm(value = "apikey") String apikey,
            @RestForm(value = "grant_type") String grantType);

    @ClientObjectMapper
    static ObjectMapper objectMapper(ObjectMapper defaultObjectMapper) {
        return QuarkusJsonCodecFactory.SnakeCaseObjectMapperHolder.MAPPER;
    }
}
