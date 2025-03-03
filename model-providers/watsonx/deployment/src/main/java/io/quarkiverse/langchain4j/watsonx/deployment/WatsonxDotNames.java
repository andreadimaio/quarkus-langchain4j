package io.quarkiverse.langchain4j.watsonx.deployment;

import org.jboss.jandex.DotName;

import io.quarkiverse.langchain4j.watsonx.tools.GoogleSearchTool;
import io.quarkiverse.langchain4j.watsonx.tools.WeatherTool;
import io.quarkiverse.langchain4j.watsonx.tools.WebCrawlerTool;

public class WatsonxDotNames {
    public static final DotName WEB_CRAWLER_TOOL = DotName.createSimple(WebCrawlerTool.class);
    public static final DotName GOOGLE_SEARCH_TOOL = DotName.createSimple(GoogleSearchTool.class);
    public static final DotName WEATHER_TOOL = DotName.createSimple(WeatherTool.class);
}
