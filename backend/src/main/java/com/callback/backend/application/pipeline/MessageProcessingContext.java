package com.callback.backend.application.pipeline;

import com.callback.backend.domain.model.PriorityLevel;
import com.callback.backend.infrastructure.persistence.entity.ContactEntity;
import com.callback.backend.infrastructure.persistence.entity.KeywordEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class MessageProcessingContext {

    private String rawPayload;
    private String sanitizedContent;
    private String senderIdentifier;
    private UUID recipientUserId;

    private ContactEntity matchedContact;
    private List<KeywordEntity> matchedKeywords = new ArrayList<>();

    private int priorityScore = 0;
    private PriorityLevel finalPriorityLevel;
}