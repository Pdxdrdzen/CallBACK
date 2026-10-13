package com.callback.backend.infrastructure.persistence.repository;

import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;
import com.callback.backend.infrastructure.persistence.entity.ContactEntity;

@Repository
public interface ContactJpaRepository {

    boolean existsByUserIdAndPhoneNumber(UUID userId, String phoneNumber);

    Optional<ContactEntity> findByUserIdAndPhoneNumber(UUID userId, String phoneNumber);

    Optional<ContactEntity> findByIdAndUserId(UUID id, UUID userId);

    Page<ContactEntity> findAllByUserId(UUID userId, Pageable pageable);
}
