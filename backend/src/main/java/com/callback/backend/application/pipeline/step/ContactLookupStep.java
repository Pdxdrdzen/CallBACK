package com.callback.backend.application.pipeline.step;

import com.callback.backend.application.pipeline.IngestionPipelineStep;
import com.callback.backend.application.pipeline.MessageProcessingContext;
import com.callback.backend.infrastructure.persistence.repository.ContactJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContactLookupStep implements IngestionPipelineStep {

    private final ContactJpaRepository contactRepository;

    @Override
    public void process(MessageProcessingContext context) {
        contactRepository.findByUserIdAndPhoneNumber(context.getRecipientUserId(), context.getSenderIdentifier())
                .ifPresent(context::setMatchedContact);
    }

    @Override
    public int getOrder() {
        return 20;
    }
}