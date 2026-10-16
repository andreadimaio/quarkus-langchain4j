package io.quarkiverse.langchain4j.watsonx.deployment;

import static io.quarkiverse.langchain4j.watsonx.deployment.WireMockUtil.BEARER_TOKEN;
import static io.quarkiverse.langchain4j.watsonx.deployment.WireMockUtil.PROJECT_ID;
import static io.quarkiverse.langchain4j.watsonx.deployment.WireMockUtil.URL_WATSONX_SERVER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.ibm.watsonx.ai.core.auth.Authenticator;
import com.ibm.watsonx.ai.detection.DetectionContextRequest;
import com.ibm.watsonx.ai.detection.DetectionContextResponse;
import com.ibm.watsonx.ai.detection.DetectionGeneratedRequest;
import com.ibm.watsonx.ai.detection.DetectionGeneratedResponse;
import com.ibm.watsonx.ai.detection.DetectionService;
import com.ibm.watsonx.ai.detection.detector.AnswerRelevance;
import com.ibm.watsonx.ai.detection.detector.ContextRelevance;
import com.ibm.watsonx.ai.detection.detector.Faithfulness;
import io.quarkus.test.QuarkusUnitTest;

public class DetectionContextGeneratedTest extends WireMockAbstract {

    @RegisterExtension
    static QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class).addClass(WireMockUtil.class));

    private static final Authenticator AUTHENTICATOR = new Authenticator() {
        @Override
        public String token() {
            return BEARER_TOKEN;
        }

        @Override
        public CompletableFuture<String> tokenAsync() {
            return CompletableFuture.completedFuture(BEARER_TOKEN);
        }

        @Override
        public String scheme() {
            return "Bearer";
        }
    };

    private DetectionService detectionService() {
        return DetectionService.builder()
                .authenticator(AUTHENTICATOR)
                .baseUrl(URL_WATSONX_SERVER)
                .projectId(PROJECT_ID)
                .build();
    }

    @Test
    void should_detect_context_relevance_and_faithfulness() {

        mockWatsonxBuilder(RequestMethod.POST, "/ml/v1/text/detection/context", 200, null)
                .body("""
                        {
                          "input": "What is IBM?",
                          "context_type": "docs",
                          "context": ["https://en.wikipedia.org/wiki/IBM"],
                          "project_id": "%s",
                          "detectors": {
                            "context_relevance": {},
                            "faithfulness": { "threshold": 0.5 }
                          }
                        }""".formatted(PROJECT_ID))
                .response("""
                        {
                          "detections": [
                            {
                              "detector_id": "context_relevance_125m_v1_en",
                              "detection_type": "context_relevance",
                              "detection": "relevant",
                              "score": 0.87,
                              "evidence": [
                                {
                                  "name": "sentence_1",
                                  "value": "IBM is a technology company.",
                                  "score": 0.92,
                                  "evidence": []
                                }
                              ]
                            }
                          ]
                        }""")
                .build();

        DetectionContextRequest request = DetectionContextRequest.builder()
                .input("What is IBM?")
                .context(List.of("https://en.wikipedia.org/wiki/IBM"))
                .detectors(ContextRelevance.ofDefaults(), Faithfulness.ofThreshold(0.5))
                .build();

        List<DetectionContextResponse> detections = detectionService().detectContext(request).detections();
        assertEquals(1, detections.size());

        DetectionContextResponse detection = detections.get(0);
        assertEquals("context_relevance_125m_v1_en", detection.detectorId());
        assertEquals("context_relevance", detection.detectionType());
        assertEquals("relevant", detection.detection());
        assertEquals(0.87, detection.score(), 0.001);
        assertEquals(1, detection.evidence().size());
        assertEquals("sentence_1", detection.evidence().get(0).name());
    }

    @Test
    void should_detect_answer_relevance_in_generated_text() {

        mockWatsonxBuilder(RequestMethod.POST, "/ml/v1/text/detection/generated", 200, null)
                .body("""
                        {
                          "prompt": "What is IBM?",
                          "generated_text": "IBM is a technology company.",
                          "project_id": "%s",
                          "detectors": {
                            "answer_relevance": {}
                          }
                        }""".formatted(PROJECT_ID))
                .response("""
                        {
                          "detections": [
                            {
                              "detector_id": "answer_relevance_125m_v1_en",
                              "detection_type": "answer_relevance",
                              "detection": "relevant",
                              "score": 0.91
                            }
                          ]
                        }""")
                .build();

        DetectionGeneratedRequest request = DetectionGeneratedRequest.builder()
                .prompt("What is IBM?")
                .generatedText("IBM is a technology company.")
                .detectors(AnswerRelevance.ofDefaults())
                .build();

        List<DetectionGeneratedResponse> detections = detectionService().detectGenerated(request).detections();
        assertEquals(1, detections.size());

        DetectionGeneratedResponse detection = detections.get(0);
        assertEquals("answer_relevance_125m_v1_en", detection.detectorId());
        assertEquals("answer_relevance", detection.detectionType());
        assertEquals("relevant", detection.detection());
        assertEquals(0.91, detection.score(), 0.001);
    }
}
