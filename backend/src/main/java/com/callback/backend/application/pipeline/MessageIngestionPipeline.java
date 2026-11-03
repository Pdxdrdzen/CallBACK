package com.callback.backend.application.pipeline;

import com.callback.backend.application.dto.IncomingMessageCommand;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class MessageIngestionPipeline {

    private final List<IngestionPipelineStep> steps;

    public MessageIngestionPipeline(List<IngestionPipelineStep> steps) {
        this.steps = steps.stream()
                .sorted(Comparator.comparingInt(IngestionPipelineStep::getOrder))
                .toList();
    }

    public MessageProcessingContext execute(IncomingMessageCommand command) {
        MessageProcessingContext context = new MessageProcessingContext();
        context.setRecipientUserId(command.recipientUserId());
        context.setSenderIdentifier(command.senderIdentifier());
        context.setRawPayload(command.rawContent());

        for (IngestionPipelineStep step : steps) {
            step.process(context);
        }

        return context;
    }
}