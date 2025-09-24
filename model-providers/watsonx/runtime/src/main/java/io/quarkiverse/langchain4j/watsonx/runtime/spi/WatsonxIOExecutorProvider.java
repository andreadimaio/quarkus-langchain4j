package io.quarkiverse.langchain4j.watsonx.runtime.spi;

import java.util.concurrent.Executor;

import com.ibm.watsonx.ai.core.spi.executor.IOExecutorProvider;

import io.smallrye.mutiny.infrastructure.Infrastructure;

public class WatsonxIOExecutorProvider implements IOExecutorProvider {

    @Override
    public Executor executor() {
        return Infrastructure.getDefaultExecutor();
    }
}
