package io.quarkiverse.langchain4j.watsonx.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.langchain4j.model.chat.DisabledChatLanguageModel;
import dev.langchain4j.model.chat.DisabledStreamingChatLanguageModel;
import dev.langchain4j.model.embedding.DisabledEmbeddingModel;
import io.quarkiverse.langchain4j.runtime.NamedConfigUtil;
import io.quarkiverse.langchain4j.watsonx.runtime.config.LangChain4jWatsonxConfig;
import io.quarkiverse.langchain4j.watsonx.runtime.config.LangChain4jWatsonxConfig.WatsonxConfig;
import io.quarkiverse.langchain4j.watsonx.runtime.config.LangChain4jWatsonxFixedRuntimeConfig;

class DisabledModelsWatsonRecorderTest {

    LangChain4jWatsonxFixedRuntimeConfig fixedRuntimeConfig = mock(LangChain4jWatsonxFixedRuntimeConfig.class);
    LangChain4jWatsonxConfig runtimeConfig = mock(LangChain4jWatsonxConfig.class);

    WatsonxConfig defaultConfig = mock(WatsonxConfig.class);
    WatsonxRecorder recorder = new WatsonxRecorder();

    @BeforeEach
    void setupMocks() {
        when(defaultConfig.enableIntegration())
                .thenReturn(false);

        when(runtimeConfig.defaultConfig())
                .thenReturn(defaultConfig);
    }

    @Test
    void disabledChatModel() {
        assertThat(recorder
                .generationModel(fixedRuntimeConfig, runtimeConfig, NamedConfigUtil.DEFAULT_NAME).apply(null))
                .isNotNull()
                .isExactlyInstanceOf(DisabledChatLanguageModel.class);

        assertThat(
                recorder.generationStreamingModel(fixedRuntimeConfig, runtimeConfig, NamedConfigUtil.DEFAULT_NAME).apply(null))
                .isNotNull()
                .isExactlyInstanceOf(DisabledStreamingChatLanguageModel.class);

        assertThat(recorder.embeddingModel(fixedRuntimeConfig, runtimeConfig, NamedConfigUtil.DEFAULT_NAME).get())
                .isNotNull()
                .isExactlyInstanceOf(DisabledEmbeddingModel.class);
    }
}
