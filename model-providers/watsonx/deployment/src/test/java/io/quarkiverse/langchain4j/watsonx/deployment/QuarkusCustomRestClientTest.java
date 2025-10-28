package io.quarkiverse.langchain4j.watsonx.deployment;

import static org.assertj.core.api.Assertions.assertThat;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.ibm.watsonx.ai.chat.ChatService;
import com.ibm.watsonx.ai.core.auth.AuthenticationProvider;
import com.ibm.watsonx.ai.core.auth.iam.IAMAuthenticator;
import com.ibm.watsonx.ai.deployment.DeploymentService;
import com.ibm.watsonx.ai.embedding.EmbeddingService;
import com.ibm.watsonx.ai.foundationmodel.FoundationModelService;
import com.ibm.watsonx.ai.rerank.RerankService;
import com.ibm.watsonx.ai.textextraction.TextExtractionService;
import com.ibm.watsonx.ai.textgeneration.TextGenerationService;
import com.ibm.watsonx.ai.timeseries.TimeSeriesService;
import com.ibm.watsonx.ai.tokenization.TokenizationService;
import com.ibm.watsonx.ai.tool.ToolService;

import io.quarkiverse.langchain4j.watsonx.runtime.client.impl.QuarkusChatRestClient;
import io.quarkiverse.langchain4j.watsonx.runtime.client.impl.QuarkusDeploymentRestClient;
import io.quarkiverse.langchain4j.watsonx.runtime.client.impl.QuarkusEmbeddingRestClient;
import io.quarkiverse.langchain4j.watsonx.runtime.client.impl.QuarkusFoundationModelRestClient;
import io.quarkiverse.langchain4j.watsonx.runtime.client.impl.QuarkusIAMRestClient;
import io.quarkiverse.langchain4j.watsonx.runtime.client.impl.QuarkusRerankRestClient;
import io.quarkiverse.langchain4j.watsonx.runtime.client.impl.QuarkusTextExtractionRestClient;
import io.quarkiverse.langchain4j.watsonx.runtime.client.impl.QuarkusTextGenerationRestClient;
import io.quarkiverse.langchain4j.watsonx.runtime.client.impl.QuarkusTimeSeriesRestClient;
import io.quarkiverse.langchain4j.watsonx.runtime.client.impl.QuarkusTokenizationRestClient;
import io.quarkiverse.langchain4j.watsonx.runtime.client.impl.QuarkusToolRestClient;
import io.quarkus.test.QuarkusUnitTest;

public class QuarkusCustomRestClientTest {

    @RegisterExtension
    static QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

    @Test
    public void iam_client() throws Exception {

        AuthenticationProvider authenticationProvider = IAMAuthenticator.builder()
                .apiKey("test")
                .build();

        Class<IAMAuthenticator> clazz = IAMAuthenticator.class;
        var clientField = clazz.getDeclaredField("client");
        clientField.setAccessible(true);
        var client = clientField.get(authenticationProvider);
        assertThat(client).isInstanceOf(QuarkusIAMRestClient.class);
    }

    @Test
    public void chat_client() throws Exception {

        ChatService chatService = ChatService.builder()
                .apiKey("test")
                .modelId("model-id")
                .baseUrl("http://localhost")
                .projectId("project-id")
                .build();

        Class<ChatService> clazz = ChatService.class;
        var clientField = clazz.getDeclaredField("client");
        clientField.setAccessible(true);
        var client = clientField.get(chatService);
        assertThat(client).isInstanceOf(QuarkusChatRestClient.class);
    }

    @Test
    public void deployment_client() throws Exception {

        DeploymentService deploymentService = DeploymentService.builder()
                .apiKey("test")
                .baseUrl("http://localhost")
                .build();

        Class<DeploymentService> clazz = DeploymentService.class;
        var clientField = clazz.getDeclaredField("client");
        clientField.setAccessible(true);
        var client = clientField.get(deploymentService);
        assertThat(client).isInstanceOf(QuarkusDeploymentRestClient.class);
    }

    @Test
    public void embedding_client() throws Exception {

        EmbeddingService embeddingService = EmbeddingService.builder()
                .apiKey("test")
                .modelId("model-id")
                .baseUrl("http://localhost")
                .projectId("project-id")
                .build();

        Class<EmbeddingService> clazz = EmbeddingService.class;
        var clientField = clazz.getDeclaredField("client");
        clientField.setAccessible(true);
        var client = clientField.get(embeddingService);
        assertThat(client).isInstanceOf(QuarkusEmbeddingRestClient.class);
    }

    @Test
    public void foundation_model_client() throws Exception {

        FoundationModelService foundationModelService = FoundationModelService.builder()
                .baseUrl("http://localhost")
                .build();

        Class<FoundationModelService> clazz = FoundationModelService.class;
        var clientField = clazz.getDeclaredField("client");
        clientField.setAccessible(true);
        var client = clientField.get(foundationModelService);
        assertThat(client).isInstanceOf(QuarkusFoundationModelRestClient.class);
    }

    @Test
    public void rerank_client() throws Exception {

        RerankService rerankService = RerankService.builder()
                .apiKey("test")
                .modelId("model-id")
                .baseUrl("http://localhost")
                .projectId("project-id")
                .build();

        Class<RerankService> clazz = RerankService.class;
        var clientField = clazz.getDeclaredField("client");
        clientField.setAccessible(true);
        var client = clientField.get(rerankService);
        assertThat(client).isInstanceOf(QuarkusRerankRestClient.class);
    }

    @Test
    public void text_extraction_client() throws Exception {

        TextExtractionService textExtractionService = TextExtractionService.builder()
                .apiKey("test")
                .cosUrl("http://localhost")
                .baseUrl("http://localhost")
                .documentReference("test", "test")
                .resultReference("test", "test")
                .projectId("project-id")
                .build();

        Class<TextExtractionService> clazz = TextExtractionService.class;
        var clientField = clazz.getDeclaredField("client");
        clientField.setAccessible(true);
        var client = clientField.get(textExtractionService);
        assertThat(client).isInstanceOf(QuarkusTextExtractionRestClient.class);
    }

    @Test
    public void text_generation_client() throws Exception {

        TextGenerationService textGenerationService = TextGenerationService.builder()
                .apiKey("test")
                .modelId("test")
                .baseUrl("http://localhost")
                .projectId("project-id")
                .build();

        Class<TextGenerationService> clazz = TextGenerationService.class;
        var clientField = clazz.getDeclaredField("client");
        clientField.setAccessible(true);
        var client = clientField.get(textGenerationService);
        assertThat(client).isInstanceOf(QuarkusTextGenerationRestClient.class);
    }

    @Test
    public void time_series_client() throws Exception {

        TimeSeriesService timeSeriesService = TimeSeriesService.builder()
                .apiKey("test")
                .modelId("test")
                .baseUrl("http://localhost")
                .projectId("project-id")
                .build();

        Class<TimeSeriesService> clazz = TimeSeriesService.class;
        var clientField = clazz.getDeclaredField("client");
        clientField.setAccessible(true);
        var client = clientField.get(timeSeriesService);
        assertThat(client).isInstanceOf(QuarkusTimeSeriesRestClient.class);
    }

    @Test
    public void tokenization_client() throws Exception {

        TokenizationService tokenizationService = TokenizationService.builder()
                .apiKey("test")
                .modelId("test")
                .baseUrl("http://localhost")
                .projectId("project-id")
                .build();

        Class<TokenizationService> clazz = TokenizationService.class;
        var clientField = clazz.getDeclaredField("client");
        clientField.setAccessible(true);
        var client = clientField.get(tokenizationService);
        assertThat(client).isInstanceOf(QuarkusTokenizationRestClient.class);
    }

    @Test
    public void tool_client() throws Exception {

        ToolService tokenizationService = ToolService.builder()
                .apiKey("test")
                .baseUrl("http://localhost")
                .build();

        Class<ToolService> clazz = ToolService.class;
        var clientField = clazz.getDeclaredField("client");
        clientField.setAccessible(true);
        var client = clientField.get(tokenizationService);
        assertThat(client).isInstanceOf(QuarkusToolRestClient.class);
    }
}
