package com.callback.backend.infrastructure.persistence.repository;

import com.callback.backend.infrastructure.persistence.entity.IncomingMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IncomingMessageJpaRepository extends JpaRepository<IncomingMessageEntity, UUID> {
    List<IncomingMessageEntity> findAllByRecipientUserIdOrderByCreatedAtDesc(UUID recipientUserId);
}