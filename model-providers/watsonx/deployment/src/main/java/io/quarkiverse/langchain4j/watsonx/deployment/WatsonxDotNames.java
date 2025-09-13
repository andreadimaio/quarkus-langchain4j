package io.quarkiverse.langchain4j.watsonx.deployment;

import org.jboss.jandex.DotName;

import com.ibm.watsonx.ai.textextraction.TextExtractionService;
import com.ibm.watsonx.ai.tool.builtin.GoogleSearchTool;
import com.ibm.watsonx.ai.tool.builtin.WeatherTool;
import com.ibm.watsonx.ai.tool.builtin.WebCrawlerTool;

public class WatsonxDotNames {
    public static final DotName WEB_CRAWLER_SERVICE = DotName.createSimple(WebCrawlerTool.class);
    public static final DotName GOOGLE_SEARCH_SERVICE = DotName.createSimple(GoogleSearchTool.class);
    public static final DotName WEATHER_SERVICE = DotName.createSimple(WeatherTool.class);
    public static final DotName TEXT_EXTRACTION = DotName.createSimple(TextExtractionService.class);
}
