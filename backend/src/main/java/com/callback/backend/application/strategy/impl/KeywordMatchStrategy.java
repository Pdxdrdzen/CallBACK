package com.callback.backend.application.strategy.impl;

import com.callback.backend.application.pipeline.MessageProcessingContext;
import com.callback.backend.application.strategy.PriorityEvaluationStrategy;
import com.callback.backend.infrastructure.persistence.entity.KeywordEntity;
import org.springframework.stereotype.Component;

@Component
public class KeywordMatchStrategy implements PriorityEvaluationStrategy {

    @Override
    public int calculateScore(MessageProcessingContext context) {
        if (context.getMatchedKeywords() == null || context.getMatchedKeywords().isEmpty()) {
            return 0;
        }

        return context.getMatchedKeywords().stream()
                .mapToInt(KeywordEntity::getWeight)
                .map(weight -> weight * 10)
                .sum();
    }
}