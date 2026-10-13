package com.callback.backend.application.service.impl;

import com.callback.backend.application.dto.ContactResponse;
import com.callback.backend.application.dto.CreateContactRequest;
import com.callback.backend.infrastructure.persistence.entity.ContactEntity;
import com.callback.backend.infrastructure.persistence.entity.UserEntity;
import com.callback.backend.infrastructure.persistence.repository.ContactJpaRepository;
import com.callback.backend.infrastructure.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.callback.backend.application.service.ContactService;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService{

    private final ContactJpaRepository contactJpaRepository;
    private final UserJpaRepository userJpaRepository;

    @Override
    @Transactional
    public ContactResponse createContact(String userEmail, CreateContactRequest req) {
        UserEntity user = userJpaRepository.findByEmail(userEmail)
                .orElseThrow(()->new IllegalArgumentException("Nie znaleziono użytkownika o podanym adresie email"));
        if(contactJpaRepository.existsByUserIdAndPhoneNumber(user.getId(),req.phoneNumber())){

        }
    }
}
