package com.callback.backend.infrastructure.web.controller;

import com.callback.backend.application.dto.ContactResponse;
import com.callback.backend.application.dto.CreateContactRequest;
import com.callback.backend.application.dto.UpdateContactRequest;
import com.callback.backend.application.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    public ResponseEntity<ContactResponse> createContact(
            @Valid @RequestBody CreateContactRequest request,
            Principal principal) {
        ContactResponse response = contactService.createContact(principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<ContactResponse>> getContacts(
            Pageable pageable,
            Principal principal) {
        // Pageable automatycznie przechwyci parametry URL np. ?page=0&size=20
        Page<ContactResponse> response = contactService.getContacts(principal.getName(), pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContactResponse> updateContact(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateContactRequest request,
            Principal principal) {
        ContactResponse response = contactService.updateContact(principal.getName(), id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(
            @PathVariable UUID id,
            Principal principal) {
        contactService.deleteContact(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }
}