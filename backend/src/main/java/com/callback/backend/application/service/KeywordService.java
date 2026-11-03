package com.callback.backend.application.service;

import com.callback.backend.application.dto.CreateKeywordRequest;
import com.callback.backend.application.dto.KeywordResponse;

import java.util.List;
import java.util.UUID;

public interface KeywordService {
    KeywordResponse createKeyword(String userEmail, CreateKeywordRequest request);
    void deleteKeyword(String userEmail, UUID keywordId);
    List<KeywordResponse> getKeywords(String userEmail);
}