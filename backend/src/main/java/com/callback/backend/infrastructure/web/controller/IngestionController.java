package com.callback.backend.infrastructure.web.controller;

import com.callback.backend.application.dto.IncomingMessageCommand;
import com.callback.backend.application.pipeline.MessageIngestionPipeline;
import com.callback.backend.application.pipeline.MessageProcessingContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ingestion")
@RequiredArgsConstructor
public class IngestionController {

    private final MessageIngestionPipeline ingestionPipeline;

    @PostMapping("/messages")
    public ResponseEntity ingestMessage(@Valid @RequestBody IncomingMessageCommand command) {
        MessageProcessingContext result = ingestionPipeline.execute(command);

        Map<String, Object> response = Map.of(
                "status", "PROCESSED",
                "calculatedPriority", result.getFinalPriorityLevel(),
                "priorityScore", result.getPriorityScore(),
                "sender", result.getSenderIdentifier()
        );

        return ResponseEntity.ok(response);
    }
}