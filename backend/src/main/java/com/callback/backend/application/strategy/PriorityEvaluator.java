package com.callback.backend.application.strategy;

import com.callback.backend.application.pipeline.MessageProcessingContext;
import com.callback.backend.domain.model.PriorityLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PriorityEvaluator {

    private final List<PriorityEvaluationStrategy> strategies;

    public void evaluate(MessageProcessingContext context) {
        int totalScore = 0;

        for (PriorityEvaluationStrategy strategy : strategies) {
            totalScore += strategy.calculateScore(context);
        }

        context.setPriorityScore(totalScore);
        context.setFinalPriorityLevel(mapToLevel(totalScore));
    }

    private PriorityLevel mapToLevel(int score) {
        if (score >= 80) return PriorityLevel.CRITICAL;
        if (score >= 50) return PriorityLevel.HIGH;
        if (score >= 25) return PriorityLevel.MEDIUM;
        return PriorityLevel.LOW;
    }
}