package io.quarkiverse.langchain4j.watsonx.bean;

import dev.langchain4j.Experimental;

@Experimental
public record WebCrawlerToolResult(String url, String contentType, String content) {
}
