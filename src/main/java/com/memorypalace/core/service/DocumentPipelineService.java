package com.memorypalace.core.service;

import com.memorypalace.core.model.Document;
import com.memorypalace.core.model.DocumentChunk;
import com.memorypalace.core.model.DocumentStatus;
import com.memorypalace.core.model.DocumentVersion;
import com.memorypalace.core.model.StoredFile;
import com.memorypalace.core.repository.document.DocumentChunkRepository;
import com.memorypalace.core.repository.document.DocumentRepository;
import com.memorypalace.core.repository.document.DocumentVersionRepository;
import com.memorypalace.core.service.enrich.EnrichmentRequest;
import com.memorypalace.core.service.enrich.EnrichmentResponse;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentPipelineService {

    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository versionRepository;
    private final DocumentChunkRepository chunkRepository;
    private final TextExtractionService textExtractionService;
    private final EnrichmentService enrichmentService;

    public DocumentPipelineService(DocumentRepository documentRepository,
                                   DocumentVersionRepository versionRepository,
                                   DocumentChunkRepository chunkRepository,
                                   TextExtractionService textExtractionService,
                                   EnrichmentService enrichmentService) {
        this.documentRepository = documentRepository;
        this.versionRepository = versionRepository;
        this.chunkRepository = chunkRepository;
        this.textExtractionService = textExtractionService;
        this.enrichmentService = enrichmentService;
    }

    @Transactional
    public DocumentVersion registerNewVersion(Document document, StoredFile storedFile) {
        int nextVersion = 1;
        Optional<DocumentVersion> last = versionRepository.findTopByDocumentOrderByVersionDesc(document.getId());
        if (last.isPresent()) {
            nextVersion = last.get().getVersionNum() + 1;
        }
        DocumentVersion v = new DocumentVersion();
        v.setDocument(document);
        v.setStoredFile(storedFile);
        v.setVersionNum(nextVersion);
        v.setStatus(DocumentStatus.UPLOADED);
        return versionRepository.save(v);
    }

    @Transactional
    public void runPipeline(DocumentVersion version, StoredFile storedFile) {
        try {
            version.setStatus(DocumentStatus.PARSING);
            versionRepository.save(version);

            List<DocumentChunk> chunks = textExtractionService.extractChunks(storedFile, 1200);
            int ord = 0;
            for (DocumentChunk c : chunks) {
                c.setVersion(version);
                c.setOrd(ord++);
                c.setTextSha256(sha256Hex(c.getText()));
                chunkRepository.save(c);
            }

            version.setStatus(DocumentStatus.ENRICHING);
            versionRepository.save(version);

            List<DocumentChunk> saved = chunkRepository.findByVersionOrdered(version.getId());
            for (DocumentChunk c : saved) {
                EnrichmentResponse er = enrichmentService.enrich(new EnrichmentRequest(c.getText(), null));
                c.setSummary(er.getSummary());
                if (er.getKeywords() != null) {
                    c.setKeywords(String.join(",", er.getKeywords()));
                }
                c.setAutoSectionTitle(er.getAutoSectionTitle());
                c.setCategory(er.getCategory());
                chunkRepository.save(c);
            }

            version.setStatus(DocumentStatus.READY);
            version.setErrorMessage(null);
            versionRepository.save(version);
        } catch (Exception e) {
            version.setStatus(DocumentStatus.FAILED);
            version.setErrorMessage(e.getMessage());
            versionRepository.save(version);
            throw e;
        }
    }

    private static String sha256Hex(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(text.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            return null;
        }
    }
}


