package com.callback.backend.application.service;

import com.callback.backend.application.dto.ContactResponse;
import com.callback.backend.application.dto.CreateContactRequest;
import com.callback.backend.application.dto.UpdateContactRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ContactService {
    ContactResponse createContact(String userEmail, CreateContactRequest req);
    ContactResponse updateContact(String userEmail, UUID contactId, UpdateContactRequest updateContactRequest);
    void deleteContact(String userEmail, UUID contactId);
    Page<ContactResponse> getContacts(String userEmail, Pageable pageable);
}
