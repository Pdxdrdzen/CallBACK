package com.callback.backend.application.strategy;

import com.callback.backend.application.pipeline.MessageProcessingContext;

public interface PriorityEvaluationStrategy {
    int calculateScore(MessageProcessingContext context);
}