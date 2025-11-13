package com.memorypalace.core.controller;

import com.memorypalace.core.model.StoredFile;
import com.memorypalace.core.repository.file.StoredFileRepository;
import com.memorypalace.core.security.TenantContext;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/files")
public class FilesQueryController {

    private final StoredFileRepository storedFileRepository;

    public FilesQueryController(StoredFileRepository storedFileRepository) {
        this.storedFileRepository = storedFileRepository;
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam("q") String query,
                                    @RequestParam(value = "projectId", required = false) UUID projectId,
                                    @RequestParam(value = "folderId", required = false) UUID folderId) {
        var principal = TenantContext.get();
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        String q = query != null ? query.trim() : "";
        List<StoredFile> list = storedFileRepository.searchByName(principal.tenantId, q, projectId, folderId);
        return ResponseEntity.ok(list.stream().map(f -> Map.of(
            "id", f.getId(),
            "title", f.getTitle(),
            "originalFilename", f.getOriginalFilename(),
            "mimeType", f.getMimeType(),
            "sizeBytes", f.getSizeBytes(),
            "createdAt", f.getCreatedAt(),
            "folderId", f.getFolder() != null ? f.getFolder().getId() : null,
            "projectId", f.getFolder() != null ? f.getFolder().getProject().getId() : null
        )).toList());
    }
}


