package com.memorypalace.core.controller;

import com.memorypalace.core.security.TenantContext;
import com.memorypalace.core.service.OpenAIEmbeddingService;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/embed")
public class EmbeddingController {

    private final OpenAIEmbeddingService embeddingService;

    public EmbeddingController(OpenAIEmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    @PostMapping("/version/{versionId}")
    public ResponseEntity<?> embedVersion(@PathVariable("versionId") UUID versionId) {
        var principal = TenantContext.get();
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        int created = embeddingService.embedVersion(versionId);
        return ResponseEntity.ok(Map.of("embedded", created, "model", "openai"));
    }
}


