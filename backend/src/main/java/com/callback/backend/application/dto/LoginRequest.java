package com.callback.backend.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Email jest wymagany")
        @Email(message = "Niepoprawny format adresu email")
        String email,

        @NotBlank(message = "Hasło jest wymagane")
        String password
) {}