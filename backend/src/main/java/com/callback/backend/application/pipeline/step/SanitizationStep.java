package com.callback.backend.application.pipeline.step;

import com.callback.backend.application.pipeline.IngestionPipelineStep;
import com.callback.backend.application.pipeline.MessageProcessingContext;
import org.springframework.stereotype.Component;

@Component
public class SanitizationStep implements IngestionPipelineStep {

    @Override
    public void process(MessageProcessingContext context) {
        if (context.getRawPayload() == null) {
            context.setSanitizedContent("");
            return;
        }

        String sanitized = context.getRawPayload()
                .replaceAll("<[^>]*>", "")
                .strip();

        context.setSanitizedContent(sanitized);
    }

    @Override
    public int getOrder() {
        return 10;
    }
}