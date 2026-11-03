package com.callback.backend.application.pipeline;

public interface IngestionPipelineStep {
    void process(MessageProcessingContext context);
    int getOrder();
}