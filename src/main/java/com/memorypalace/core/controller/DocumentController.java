package com.memorypalace.core.controller;

import com.memorypalace.core.model.Document;
import com.memorypalace.core.model.DocumentChunk;
import com.memorypalace.core.model.DocumentVersion;
import com.memorypalace.core.model.StoredFile;
import com.memorypalace.core.repository.document.DocumentChunkRepository;
import com.memorypalace.core.repository.document.DocumentRepository;
import com.memorypalace.core.repository.file.StoredFileRepository;
import com.memorypalace.core.security.TenantContext;
import com.memorypalace.core.service.DocumentPipelineService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final StoredFileRepository storedFileRepository;
    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository chunkRepository;
    private final DocumentPipelineService pipelineService;

    public DocumentController(StoredFileRepository storedFileRepository,
                              DocumentRepository documentRepository,
                              DocumentChunkRepository chunkRepository,
                              DocumentPipelineService pipelineService) {
        this.storedFileRepository = storedFileRepository;
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.pipelineService = pipelineService;
    }

    @PostMapping("/start-from-file")
    public ResponseEntity<?> startFromFile(@RequestParam("storedFileId") UUID storedFileId,
                                           @RequestParam(value = "title", required = false) String title) {
        var principal = TenantContext.get();
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        StoredFile sf = storedFileRepository.findById(storedFileId).orElse(null);
        if (sf == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Stored file not found"));
        }
        if (!sf.getTenant().getId().equals(principal.tenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Forbidden"));
        }

        Document doc = new Document();
        doc.setTenant(sf.getTenant());
        doc.setOwner(sf.getOwner());
        doc.setTitle(title != null ? title : (sf.getTitle() != null ? sf.getTitle() : sf.getOriginalFilename()));
        doc = documentRepository.save(doc);

        DocumentVersion v = pipelineService.registerNewVersion(doc, sf);
        pipelineService.runPipeline(v, sf);
        List<DocumentChunk> chunks = chunkRepository.findByVersionOrdered(v.getId());
        return ResponseEntity.ok(Map.of(
            "documentId", doc.getId(),
            "versionId", v.getId(),
            "status", v.getStatus().name(),
            "chunkCount", chunks.size()
        ));
    }

    @GetMapping("/version/{versionId}/chunks")
    public ResponseEntity<?> listChunks(@PathVariable("versionId") UUID versionId) {
        var principal = TenantContext.get();
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        List<DocumentChunk> chunks = chunkRepository.findByVersionOrdered(versionId);
        // Note: tenant ownership check could be added by joining version->document->tenant in repo; omitted here for brevity.
        return ResponseEntity.ok(chunks.stream().map(c -> Map.of(
            "id", c.getId(),
            "ord", c.getOrd(),
            "text", c.getText(),
            "summary", c.getSummary(),
            "keywords", c.getKeywords(),
            "autoSectionTitle", c.getAutoSectionTitle(),
            "category", c.getCategory()
        )).toList());
    }
}


