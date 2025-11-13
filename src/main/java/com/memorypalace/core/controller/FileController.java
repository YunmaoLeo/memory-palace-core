package com.memorypalace.core.controller;

import com.memorypalace.core.dto.FileUploadResponse;
import com.memorypalace.core.model.StoredFile;
import com.memorypalace.core.repository.user.UserRepository;
import com.memorypalace.core.security.TenantContext;
import com.memorypalace.core.service.FileStorageService;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;

    public FileController(FileStorageService fileStorageService, UserRepository userRepository) {
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> upload(@RequestParam("file") MultipartFile file,
                                                     @RequestParam(value = "title", required = false) String title)
        throws Exception {
        var principal = TenantContext.get();
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        UUID userId = principal.userId;
        var owner = userRepository.findById(userId).orElseThrow();

        var result = fileStorageService.upload(owner, file, title);
        StoredFile f = result.storedFile;
        FileUploadResponse resp = new FileUploadResponse(
            f.getId(), f.getTenant().getId(), f.getOwner().getId(),
            f.getTitle(), f.getOriginalFilename(), f.getMimeType(), f.getSizeBytes(),
            f.getSha256Hex(), f.getS3Bucket(), f.getS3Key(), f.getCreatedAt(),
            result.duplicate
        );
        return ResponseEntity.ok(resp);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(java.util.Map.of("error", ex.getMessage()));
    }
}


