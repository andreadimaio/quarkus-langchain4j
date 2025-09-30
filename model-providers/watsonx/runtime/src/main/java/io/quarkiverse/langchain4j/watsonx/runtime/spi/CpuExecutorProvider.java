package io.quarkiverse.langchain4j.watsonx.runtime.spi;

import java.util.concurrent.Executor;

import io.smallrye.mutiny.infrastructure.Infrastructure;

public class CpuExecutorProvider implements com.ibm.watsonx.ai.core.spi.executor.CpuExecutorProvider {

    @Override
    public Executor executor() {
        return Infrastructure.getDefaultWorkerPool();
    }
}
