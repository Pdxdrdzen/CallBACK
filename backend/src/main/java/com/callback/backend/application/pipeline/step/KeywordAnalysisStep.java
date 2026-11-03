package com.callback.backend.application.pipeline.step;

import com.callback.backend.application.pipeline.IngestionPipelineStep;
import com.callback.backend.application.pipeline.MessageProcessingContext;
import com.callback.backend.infrastructure.persistence.entity.KeywordEntity;
import com.callback.backend.infrastructure.persistence.repository.KeywordJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class KeywordAnalysisStep implements IngestionPipelineStep {

    private final KeywordJpaRepository keywordRepository;

    @Override
    public void process(MessageProcessingContext context) {
        String content = context.getSanitizedContent().toLowerCase();
        List<KeywordEntity> userKeywords = keywordRepository.findAllByUserId(context.getRecipientUserId());

        List<KeywordEntity> matched = userKeywords.stream()
                .filter(k -> content.contains(k.getKeyword().toLowerCase()))
                .toList();

        context.setMatchedKeywords(matched);
    }

    @Override
    public int getOrder() {
        return 30;
    }
}