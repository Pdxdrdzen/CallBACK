package com.callback.backend.application.service.impl;

import com.callback.backend.application.dto.ContactResponse;
import com.callback.backend.application.dto.CreateContactRequest;
import com.callback.backend.application.dto.UpdateContactRequest;
import com.callback.backend.application.service.ContactService;
import com.callback.backend.infrastructure.persistence.entity.ContactEntity;
import com.callback.backend.infrastructure.persistence.entity.UserEntity;
import com.callback.backend.infrastructure.persistence.repository.ContactJpaRepository;
import com.callback.backend.infrastructure.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private final ContactJpaRepository contactRepository;
    private final UserJpaRepository userRepository;
    private static final String NO_USER = "Nie znaleziono użytkownika";

    @Override
    @Transactional
    public ContactResponse createContact(String userEmail, CreateContactRequest req) {
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException(NO_USER));

        if (contactRepository.existsByUserIdAndPhoneNumber(user.getId(), req.phoneNumber())) {
            throw new IllegalArgumentException("Kontakt z podanym numerem telefonu już istnieje.");
        }

        ContactEntity contact = ContactEntity.builder()
                .user(user)
                .name(req.name())
                .phoneNumber(req.phoneNumber())
                .email(req.email())
                .priorityRole(req.priorityRole())
                .build();

        ContactEntity savedContact = contactRepository.save(contact);

        return mapToResponse(savedContact);
    }

    @Override
    @Transactional
    public ContactResponse updateContact(String userEmail, UUID contactId, UpdateContactRequest req) {
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException(NO_USER));

        ContactEntity contact = contactRepository.findByIdAndUserId(contactId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono kontaktu lub brak uprawnień"));

        contact.setName(req.name());
        contact.setPhoneNumber(req.phoneNumber());
        contact.setEmail(req.email());
        contact.setPriorityRole(req.priorityRole());

        return mapToResponse(contactRepository.save(contact));
    }

    @Override
    @Transactional
    public void deleteContact(String userEmail, UUID contactId) {
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException(NO_USER));

        ContactEntity contact = contactRepository.findByIdAndUserId(contactId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono kontaktu lub brak uprawnień"));

        contactRepository.delete(contact);
    }

    @Override
    public Page<ContactResponse> getContacts(String userEmail, Pageable pageable) {
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException(NO_USER));

        return contactRepository.findAllByUserId(user.getId(), pageable)
                .map(this::mapToResponse);
    }

    private ContactResponse mapToResponse(ContactEntity entity) {
        return new ContactResponse(
                entity.getId(),
                entity.getName(),
                entity.getPhoneNumber(),
                entity.getEmail(),
                entity.getPriorityRole()
        );
    }
}