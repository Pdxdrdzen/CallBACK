package com.callback.backend.application.pipeline.step;

import com.callback.backend.application.pipeline.IngestionPipelineStep;
import com.callback.backend.application.pipeline.MessageProcessingContext;
import com.callback.backend.infrastructure.persistence.entity.IncomingMessageEntity;
import com.callback.backend.infrastructure.persistence.entity.OutboxEventEntity;
import com.callback.backend.infrastructure.persistence.entity.UserEntity;
import com.callback.backend.infrastructure.persistence.repository.IncomingMessageJpaRepository;
import com.callback.backend.infrastructure.persistence.repository.OutboxEventJpaRepository;
import com.callback.backend.infrastructure.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OutboxPersistenceStep implements IngestionPipelineStep {

    private final IncomingMessageJpaRepository incomingMessageRepository;
    private final OutboxEventJpaRepository outboxEventRepository;
    private final UserJpaRepository userRepository;

    @Override
    @Transactional
    public void process(MessageProcessingContext context) {
        UserEntity recipient = userRepository.findById(context.getRecipientUserId())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono odbiorcy komunikatu"));

        IncomingMessageEntity message = IncomingMessageEntity.builder()
                .recipientUser(recipient)
                .senderIdentifier(context.getSenderIdentifier())
                .rawContent(context.getRawPayload())
                .sanitizedContent(context.getSanitizedContent())
                .calculatedPriority(context.getFinalPriorityLevel())
                .status("RECEIVED")
                .build();

        IncomingMessageEntity savedMessage = incomingMessageRepository.save(message);

        String jsonPayload = String.format(
                "{\"messageId\":\"%s\",\"recipientUserId\":\"%s\",\"priority\":\"%s\",\"score\":%d}",
                savedMessage.getId(),
                recipient.getId(),
                context.getFinalPriorityLevel(),
                context.getPriorityScore()
        );

        OutboxEventEntity outboxEvent = OutboxEventEntity.builder()
                .aggregateType("INCOMING_MESSAGE")
                .aggregateId(savedMessage.getId().toString())
                .eventType("MESSAGE_RECEIVED")
                .payload(jsonPayload)
                .processed(false)
                .build();

        outboxEventRepository.save(outboxEvent);
    }

    @Override
    public int getOrder() {
        return 50;
    }
}