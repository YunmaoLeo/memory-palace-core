package com.memorypalace.core.service.impl;

import com.memorypalace.core.model.DocumentChunk;
import com.memorypalace.core.model.Modality;
import com.memorypalace.core.model.StoredFile;
import com.memorypalace.core.service.TextExtractionService;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

@Service
public class PdfExtractionService implements TextExtractionService {

    private final S3Client s3Client;

    public PdfExtractionService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public List<DocumentChunk> extractChunks(StoredFile storedFile, int approximateChunkSize) {
        String mime = storedFile.getMimeType() != null ? storedFile.getMimeType() : "";
        String name = storedFile.getOriginalFilename() != null ? storedFile.getOriginalFilename().toLowerCase() : "";
        if (!"application/pdf".equalsIgnoreCase(mime) && !name.endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF supported by PdfExtractionService");
        }

        byte[] pdfBytes = s3Client.getObject(
            GetObjectRequest.builder()
                .bucket(storedFile.getS3Bucket())
                .key(storedFile.getS3Key())
                .build(),
            ResponseTransformer.toBytes()
        ).asByteArray();

        try (InputStream in = new java.io.ByteArrayInputStream(pdfBytes);
             PDDocument doc = PDDocument.load(in)) {
            int pages = doc.getNumberOfPages();
            List<DocumentChunk> chunks = new ArrayList<>();
            PDFTextStripper stripper = new PDFTextStripper();
            StringBuilder buf = new StringBuilder();
            int pageStartForBuf = -1;
            int chunkSize = Math.max(approximateChunkSize, 800);
            for (int p = 1; p <= pages; p++) {
                stripper.setStartPage(p);
                stripper.setEndPage(p);
                String pageText = stripper.getText(doc);
                if (pageText == null) pageText = "";
                if (pageStartForBuf == -1) pageStartForBuf = p;
                // Paragraph-aware packing using same strategy as TXT
                List<String> paragraphs = PlainTextExtractionService.toParagraphs(pageText);
                for (String para : paragraphs) {
                    String pstr = para.strip();
                    if (pstr.isEmpty()) continue;
                    if (pstr.length() > chunkSize) {
                        // flush
                        if (buf.length() > 0) {
                            chunks.add(buildTextChunk(buf.toString(), pageStartForBuf, p));
                            buf.setLength(0);
                            pageStartForBuf = p;
                        }
                        // sentence fallback
                        List<DocumentChunk> tmp = new ArrayList<>();
                        PlainTextExtractionService.packSentences(pstr, chunkSize, tmp);
                        for (DocumentChunk c : tmp) {
                            c.setPageStart(Integer.valueOf(p));
                            c.setPageEnd(Integer.valueOf(p));
                            chunks.add(c);
                        }
                        continue;
                    }
                    if (buf.length() > 0 && buf.length() + 2 + pstr.length() > chunkSize) {
                        chunks.add(buildTextChunk(buf.toString(), pageStartForBuf, p));
                        buf.setLength(0);
                        pageStartForBuf = p;
                    }
                    if (buf.length() > 0) buf.append("\n\n");
                    buf.append(pstr);
                }
            }
            if (buf.length() > 0) {
                chunks.add(buildTextChunk(buf.toString(), pageStartForBuf, pages));
            }
            return chunks;
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse PDF", e);
        }
    }

    private static DocumentChunk buildTextChunk(String text, int pageStart, int pageEnd) {
        DocumentChunk c = new DocumentChunk();
        c.setModality(Modality.TEXT);
        c.setPageStart(Integer.valueOf(pageStart));
        c.setPageEnd(Integer.valueOf(pageEnd));
        c.setText(text);
        c.setTokenCount(Integer.valueOf(Math.max(1, text.split("\\s+").length)));
        return c;
    }
}


