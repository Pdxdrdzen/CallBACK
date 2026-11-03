package com.callback.backend.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record IncomingMessageCommand(
        @NotNull(message = "ID odbiorcy jest wymagane")
        UUID recipientUserId,

        @NotBlank(message = "Identyfikator nadawcy nie może być pusty")
        String senderIdentifier,

        @NotBlank(message = "Treść wiadomości nie może być pusta")
        String rawContent
) {}