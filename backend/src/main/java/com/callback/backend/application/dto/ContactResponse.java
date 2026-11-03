package com.callback.backend.application.dto;

import com.callback.backend.domain.model.PriorityRole;
import java.util.UUID;

public record ContactResponse(
        UUID id,
        String name,
        String phoneNumber,
        String email,
        PriorityRole priorityRole
) {}
