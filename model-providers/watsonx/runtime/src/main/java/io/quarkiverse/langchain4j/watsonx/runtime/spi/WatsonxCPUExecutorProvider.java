package io.quarkiverse.langchain4j.watsonx.runtime.spi;

import java.util.concurrent.Executor;

import com.ibm.watsonx.ai.core.spi.executor.CpuExecutorProvider;

import io.smallrye.mutiny.infrastructure.Infrastructure;

public class WatsonxCPUExecutorProvider implements CpuExecutorProvider {

    @Override
    public Executor executor() {
        Infrastructure.getDefaultExecutor();
        return Infrastructure.getDefaultWorkerPool();
    }
}
