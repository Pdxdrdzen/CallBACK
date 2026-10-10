package com.callback.backend.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Email jest wymagany")
        @Email(message = "Niepoprawny format adresu email. Spróbuj ponownie")
        String email,

        @NotBlank(message = "Hasło jest wymagane")
        @Size(min = 8, max = 100, message = "Hasło musi mieć minimum 8 znaków")
        String password
) {}