package com.callback.backend.application.dto;

import jakarta.validation.constraints.*;
import com.callback.backend.domain.model.PriorityRole;

public record CreateContactRequest(
        @NotBlank(message="Pole jest wymagane")
        @Size(max=30,message="Wpisz poprawne imię")
        String name,

        @NotBlank(message="Pole jest wymagane")
        @Size(max=15,message="Wprowadź poprawny numer telefonu")
        @Pattern(regexp = "^\\+?[0-9\\s\\-]{7,15}$",message="Wprowadż poprawny numer telefonu")
        String phoneNumber,

        @Email(message="Wprowadź poprawny adres email")
        String email,

        @NotNull(message = "Wybierz role dla kontaktu")
        PriorityRole priorityRole

) { }