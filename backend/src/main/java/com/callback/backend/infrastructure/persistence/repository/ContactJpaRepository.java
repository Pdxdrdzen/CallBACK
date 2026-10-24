package com.callback.backend.infrastructure.persistence.repository;

import com.callback.backend.infrastructure.persistence.entity.ContactEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContactJpaRepository extends JpaRepository<ContactEntity, UUID> { // <--- Tutaj była luka

    boolean existsByUserIdAndPhoneNumber(UUID userId, String phoneNumber);

    Optional<ContactEntity> findByUserIdAndPhoneNumber(UUID userId, String phoneNumber);

    Optional<ContactEntity> findByIdAndUserId(UUID id, UUID userId);

    Page<ContactEntity> findAllByUserId(UUID userId, Pageable pageable);
}