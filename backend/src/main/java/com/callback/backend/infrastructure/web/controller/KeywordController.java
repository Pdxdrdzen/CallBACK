package com.callback.backend.infrastructure.web.controller;

import com.callback.backend.application.dto.CreateKeywordRequest;
import com.callback.backend.application.dto.KeywordResponse;
import com.callback.backend.application.service.KeywordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/keywords")
@RequiredArgsConstructor
public class KeywordController {

    private final KeywordService keywordService;

    @PostMapping("/create")
    public ResponseEntity<KeywordResponse> createKeyword(
            @Valid @RequestBody CreateKeywordRequest request,
            Principal principal) {
        KeywordResponse response = keywordService.createKeyword(principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/get")
    public ResponseEntity<List<KeywordResponse>> getKeywords(Principal principal) {
        List<KeywordResponse> response = keywordService.getKeywords(principal.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteKeyword(
            @PathVariable UUID id,
            Principal principal) {
        keywordService.deleteKeyword(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }
}