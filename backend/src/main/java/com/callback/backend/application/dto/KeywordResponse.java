package com.callback.backend.application.dto;

import java.util.UUID;

public record KeywordResponse(
        UUID id,
        String keyword,
        Integer weight
) {}