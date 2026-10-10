package com.callback.backend.infrastructure.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @GetMapping("/protected")
    public ResponseEntity<String> protectedEndpoint(Principal principal) {
        return ResponseEntity.ok("Sukces! Jesteś uwierzytelniony jako: " + principal.getName());
    }
}