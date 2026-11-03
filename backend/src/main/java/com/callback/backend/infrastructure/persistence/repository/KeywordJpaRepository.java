package com.callback.backend.infrastructure.persistence.repository;

import com.callback.backend.infrastructure.persistence.entity.KeywordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface KeywordJpaRepository extends JpaRepository<KeywordEntity, UUID> {
    boolean existsByUserIdAndKeywordIgnoreCase(UUID userId, String keyword);
    Optional<KeywordEntity> findByIdAndUserId(UUID id, UUID userId);
    List<KeywordEntity> findAllByUserId(UUID userId);
}