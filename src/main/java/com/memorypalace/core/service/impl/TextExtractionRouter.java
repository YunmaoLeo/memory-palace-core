package com.memorypalace.core.service.impl;

import com.memorypalace.core.model.StoredFile;
import com.memorypalace.core.service.TextExtractionService;
import java.util.List;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class TextExtractionRouter implements TextExtractionService {

    private final PlainTextExtractionService plain;
    private final PdfExtractionService pdf;

    public TextExtractionRouter(PlainTextExtractionService plain, PdfExtractionService pdf) {
        this.plain = plain;
        this.pdf = pdf;
    }

    @Override
    public List<com.memorypalace.core.model.DocumentChunk> extractChunks(StoredFile storedFile, int approximateChunkSize) {
        String mime = storedFile.getMimeType() != null ? storedFile.getMimeType() : "";
        String name = storedFile.getOriginalFilename() != null ? storedFile.getOriginalFilename().toLowerCase() : "";
        if ("text/plain".equalsIgnoreCase(mime) || name.endsWith(".txt")) {
            return plain.extractChunks(storedFile, approximateChunkSize);
        }
        if ("application/pdf".equalsIgnoreCase(mime) || name.endsWith(".pdf")) {
            return pdf.extractChunks(storedFile, approximateChunkSize);
        }
        throw new IllegalArgumentException("Unsupported MIME/extension: " + mime + " / " + name);
    }
}


