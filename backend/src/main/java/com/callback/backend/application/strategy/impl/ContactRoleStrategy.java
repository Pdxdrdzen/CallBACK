package com.callback.backend.application.strategy.impl;

import com.callback.backend.application.pipeline.MessageProcessingContext;
import com.callback.backend.application.strategy.PriorityEvaluationStrategy;
import org.springframework.stereotype.Component;

@Component
public class ContactRoleStrategy implements PriorityEvaluationStrategy {

    @Override
    public int calculateScore(MessageProcessingContext context) {
        if (context.getMatchedContact() == null || context.getMatchedContact().getPriorityRole() == null) {
            return 5;
        }

        return switch (context.getMatchedContact().getPriorityRole()) {
            case FAMILY -> 45;
            case MANAGEMENT -> 40;
            case CLIENT -> 25;
            case STANDARD -> 10;
        };
    }
}