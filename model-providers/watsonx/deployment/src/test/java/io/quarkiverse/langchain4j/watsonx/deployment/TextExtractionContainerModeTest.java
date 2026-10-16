package io.quarkiverse.langchain4j.watsonx.deployment;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.http.RequestMethod.PUT;
import static io.quarkiverse.langchain4j.watsonx.deployment.WireMockUtil.API_KEY;
import static io.quarkiverse.langchain4j.watsonx.deployment.WireMockUtil.BEARER_TOKEN;
import static io.quarkiverse.langchain4j.watsonx.deployment.WireMockUtil.PROJECT_ID;
import static io.quarkiverse.langchain4j.watsonx.deployment.WireMockUtil.URL_COS_SERVER;
import static io.quarkiverse.langchain4j.watsonx.deployment.WireMockUtil.URL_IAM_SERVER;
import static io.quarkiverse.langchain4j.watsonx.deployment.WireMockUtil.URL_WATSONX_SERVER;
import static io.quarkiverse.langchain4j.watsonx.deployment.WireMockUtil.URL_WATSONX_TEXT_EXTRACTION_START_API;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Path;
import java.util.Date;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.ibm.watsonx.ai.textprocessing.textextraction.TextExtractionResponse;
import com.ibm.watsonx.ai.textprocessing.textextraction.TextExtractionService;

import io.quarkus.test.QuarkusUnitTest;

public class TextExtractionContainerModeTest extends WireMockAbstract {

    static final String FILE_NAME = "test.pdf";
    static final String PROCESS_EXTRACTION_ID = "my-id";
    static final String RESOLVED_BUCKET_NAME = "project-default-bucket";

    @RegisterExtension
    static QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.base-url", URL_WATSONX_SERVER)
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.iam.base-url", URL_IAM_SERVER)
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.api-key", API_KEY)
            .overrideRuntimeConfigKey("quarkus.langchain4j.watsonx.project-id", PROJECT_ID)
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClass(WireMockUtil.class)
                    .addAsResource(FILE_NAME));

    @Override
    void handlerBeforeEach() {
        mockIAMBuilder(200)
                .grantType(langchain4jWatsonConfig.defaultConfig().iam().grantType().orElse(null))
                .response(BEARER_TOKEN, new Date())
                .build();
    }

    @Inject
    TextExtractionService textExtraction;

    @Test
    void uploadAndStartExtraction_usesProjectDefaultContainer() throws Exception {
        var file = Path.of(ClassLoader.getSystemResource(FILE_NAME).toURI()).toFile();

        watsonxServer.stubFor(get(urlEqualTo("/v2/projects/" + PROJECT_ID))
                .withHeader("Authorization", equalTo("Bearer " + BEARER_TOKEN))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "metadata": {
                                    "guid": "%s",
                                    "url": "/v2/projects/%s",
                                    "created_at": "2026-01-01T00:00:00.000Z",
                                    "updated_at": "2026-01-01T00:00:00.000Z"
                                  },
                                  "entity": {
                                    "name": "test-project",
                                    "storage": {
                                      "type": "bmcos_object_storage",
                                      "guid": "storage-guid",
                                      "properties": {
                                        "bucket_name": "%s",
                                        "endpoint_url": "%s"
                                      }
                                    }
                                  }
                                }
                                """.formatted(PROJECT_ID, PROJECT_ID, RESOLVED_BUCKET_NAME, URL_COS_SERVER))));

        mockCosBuilder(PUT, RESOLVED_BUCKET_NAME, FILE_NAME, 200).build();

        mockTextExtractionBuilder(POST, URL_WATSONX_TEXT_EXTRACTION_START_API, 200)
                .response("""
                        {
                          "metadata": {
                            "id": "%s",
                            "created_at": "2026-01-01T00:00:00.000Z",
                            "project_id": "%s",
                            "name": "extract"
                          },
                          "entity": {
                            "results": {
                              "status": "submitted",
                              "number_pages_processed": 0
                            }
                          }
                        }
                        """.formatted(PROCESS_EXTRACTION_ID, PROJECT_ID))
                .build();

        TextExtractionResponse response = textExtraction.uploadAndStartExtraction(file);

        assertNotNull(response);
        assertEquals(PROCESS_EXTRACTION_ID, response.metadata().id());
        assertEquals("submitted", response.entity().results().status());

        watsonxServer.verify(1, getRequestedFor(urlEqualTo("/v2/projects/" + PROJECT_ID)));
        cosServer.verify(1, putRequestedFor(urlEqualTo("/" + RESOLVED_BUCKET_NAME + "/" + FILE_NAME)));
    }
}
