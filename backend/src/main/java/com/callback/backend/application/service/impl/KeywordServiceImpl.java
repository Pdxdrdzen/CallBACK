package com.callback.backend.application.service.impl;

import com.callback.backend.application.dto.CreateKeywordRequest;
import com.callback.backend.application.dto.KeywordResponse;
import com.callback.backend.application.service.KeywordService;
import com.callback.backend.infrastructure.persistence.entity.KeywordEntity;
import com.callback.backend.infrastructure.persistence.entity.UserEntity;
import com.callback.backend.infrastructure.persistence.repository.KeywordJpaRepository;
import com.callback.backend.infrastructure.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KeywordServiceImpl implements KeywordService {

    private final KeywordJpaRepository keywordRepository;
    private final UserJpaRepository userRepository;

    private static final String NO_USER="Nie znaleziono użytkownika";

    @Override
    @Transactional
    public KeywordResponse createKeyword(String userEmail, CreateKeywordRequest request) {
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException(NO_USER));

        String normalizedWord = request.keyword().trim().toLowerCase();

        if (keywordRepository.existsByUserIdAndKeywordIgnoreCase(user.getId(), normalizedWord)) {
            throw new IllegalArgumentException("To słowo kluczowe już istnieje.");
        }

        KeywordEntity keyword = KeywordEntity.builder()
                .user(user)
                .keyword(normalizedWord)
                .weight(request.weight())
                .build();

        return mapToResponse(keywordRepository.save(keyword));
    }

    @Override
    @Transactional
    public void deleteKeyword(String userEmail, UUID keywordId) {
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException(NO_USER));

        KeywordEntity keyword = keywordRepository.findByIdAndUserId(keywordId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono słowa lub brak uprawnień"));

        keywordRepository.delete(keyword);
    }

    @Override
    public List<KeywordResponse> getKeywords(String userEmail) {
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException(NO_USER));

        return keywordRepository.findAllByUserId(user.getId()).stream()
                .map(this::mapToResponse)
                .toList();
    }

    private KeywordResponse mapToResponse(KeywordEntity entity) {
        return new KeywordResponse(entity.getId(), entity.getKeyword(), entity.getWeight());
    }
}