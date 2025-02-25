package io.quarkiverse.langchain4j.watsonx.tools;

import static io.quarkiverse.langchain4j.watsonx.WatsonxUtils.retryOn;
import static io.quarkiverse.langchain4j.watsonx.bean.UtilityAgentToolsRequest.ToolName.GOOGLE_SEARCH;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.Experimental;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import io.quarkiverse.langchain4j.QuarkusJsonCodecFactory;
import io.quarkiverse.langchain4j.watsonx.bean.GoogleSearchToolResult;
import io.quarkiverse.langchain4j.watsonx.bean.UtilityAgentToolsRequest;
import io.quarkiverse.langchain4j.watsonx.bean.UtilityAgentToolsRequest.StringInput;
import io.quarkiverse.langchain4j.watsonx.bean.UtilityAgentToolsResponse;
import io.quarkiverse.langchain4j.watsonx.client.UtilityAgentToolsRestApi;
import io.quarkiverse.langchain4j.watsonx.exception.BuiltinToolException;

/**
 * Builtin tool to search for online trends, news, current events, real-time information or research topics.
 */
@Experimental
public class GoogleSearchTool {

    private static final Logger logger = Logger.getLogger(GoogleSearchTool.class);
    private static final ObjectMapper objectMapper = QuarkusJsonCodecFactory.ObjectMapperHolder.MAPPER;
    private UtilityAgentToolsRestApi client;

    public GoogleSearchTool(UtilityAgentToolsRestApi utilityAgentToolRestApi) {
        this.client = utilityAgentToolRestApi;
    }

    /**
     * Search for online trends, news, current events, real-time information, or research topics.
     *
     * @param url The URL of the web page to fetch.
     * @return A {@code WebCrawlerToolResult} containing the retrieved web page content.
     */
    @Tool(value = "Search for online trends, news, current events, real-time information, or research topics")
    public List<GoogleSearchToolResult> search(
            @P(required = true, value = "Text to search") String input,
            @P(required = false, value = "Max number of results to return") Integer maxResults) throws BuiltinToolException {

        if (Objects.isNull(input) || input.isBlank())
            throw new IllegalArgumentException("The field \"input\" cannot be null or empty");

        if (maxResults == null || maxResults < 0) {
            maxResults = 1;
        } else if (maxResults > 20) {
            logger.info("The tool cannot return more than 20 elements, set maxResults to 20");
            maxResults = 20;
        }

        var request = new UtilityAgentToolsRequest(GOOGLE_SEARCH, new StringInput(input), Map.of("maxResults", maxResults));
        var response = retryOn(new Callable<UtilityAgentToolsResponse>() {
            @Override
            public UtilityAgentToolsResponse call() throws Exception {
                return client.run(request);
            }
        });

        try {
            return objectMapper.readValue(response.output(), new TypeReference<List<GoogleSearchToolResult>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
