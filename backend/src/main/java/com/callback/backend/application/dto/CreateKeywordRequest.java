package com.callback.backend.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateKeywordRequest(
        @NotBlank(message = "Słowo kluczowe nie może być puste")
        @Size(max = 50, message = "Słowo kluczowe może mieć maksymalnie 50 znaków")
        String word,

        @NotNull(message = "Waga jest wymagana")
        @Min(value = 1, message = "Minimalna waga to 1")
        @Max(value = 5, message = "Maksymalna waga to 5")
        Integer weight
) {}