package com.callback.backend.application.pipeline.step;

import com.callback.backend.application.pipeline.IngestionPipelineStep;
import com.callback.backend.application.pipeline.MessageProcessingContext;
import com.callback.backend.application.strategy.PriorityEvaluator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PriorityCalculationStep implements IngestionPipelineStep {

    private final PriorityEvaluator priorityEvaluator;

    @Override
    public void process(MessageProcessingContext context) {
        priorityEvaluator.evaluate(context);
    }

    @Override
    public int getOrder() {
        return 40;
    }
}