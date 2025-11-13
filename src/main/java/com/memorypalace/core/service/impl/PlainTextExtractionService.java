package com.memorypalace.core.service.impl;

import com.memorypalace.core.model.DocumentChunk;
import com.memorypalace.core.model.StoredFile;
import com.memorypalace.core.service.TextExtractionService;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

@Service
public class PlainTextExtractionService implements TextExtractionService {

    private final S3Client s3Client;

    public PlainTextExtractionService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public List<DocumentChunk> extractChunks(StoredFile storedFile, int approximateChunkSize) {
        String mime = storedFile.getMimeType() != null ? storedFile.getMimeType() : "";
        String name = storedFile.getOriginalFilename() != null ? storedFile.getOriginalFilename().toLowerCase() : "";
        if (!"text/plain".equalsIgnoreCase(mime) && !name.endsWith(".txt")) {
            throw new IllegalArgumentException("Only text/plain supported in this stage");
        }

        String text = s3Client.getObject(
            GetObjectRequest.builder()
                .bucket(storedFile.getS3Bucket())
                .key(storedFile.getS3Key())
                .build(),
            ResponseTransformer.toBytes()
        ).asString(StandardCharsets.UTF_8);

        int targetSize = Math.max(approximateChunkSize, 800);
        return splitParagraphAware(text, targetSize);
    }

    static List<DocumentChunk> splitParagraphAware(String text, int targetSize) {
        List<DocumentChunk> result = new ArrayList<>();
        List<String> paragraphs = toParagraphs(text);
        StringBuilder buf = new StringBuilder();
        for (String para : paragraphs) {
            String p = para.strip();
            if (p.isEmpty()) continue;
            if (p.length() > targetSize) {
                // flush current buffer first
                if (buf.length() > 0) {
                    result.add(buildChunk(buf.toString()));
                    buf.setLength(0);
                }
                // sentence-level fallback packing
                packSentences(p, targetSize, result);
                continue;
            }
            if (buf.length() > 0 && buf.length() + 1 + p.length() > targetSize) {
                result.add(buildChunk(buf.toString()));
                buf.setLength(0);
            }
            if (buf.length() > 0) buf.append("\n\n");
            buf.append(p);
        }
        if (buf.length() > 0) {
            result.add(buildChunk(buf.toString()));
        }
        return result;
    }

    static List<String> toParagraphs(String text) {
        List<String> paras = new ArrayList<>();
        String[] lines = text.split("\\r?\\n");
        StringBuilder current = new StringBuilder();
        for (String line : lines) {
            if (line.strip().isEmpty()) {
                if (current.length() > 0) {
                    paras.add(current.toString());
                    current.setLength(0);
                }
            } else {
                if (current.length() > 0) current.append('\n');
                current.append(line);
            }
        }
        if (current.length() > 0) paras.add(current.toString());
        return paras;
    }

    static void packSentences(String paragraph, int targetSize, List<DocumentChunk> out) {
        // Rough sentence split for English and Chinese punctuation.
        String[] sentences = paragraph.split("(?<=[.!?。！？])\\s+");
        StringBuilder buf = new StringBuilder();
        for (String s : sentences) {
            String seg = s.strip();
            if (seg.isEmpty()) continue;
            if (seg.length() > targetSize) {
                // hard cut if even a single sentence is too long
                int start = 0;
                while (start < seg.length()) {
                    int end = Math.min(start + targetSize, seg.length());
                    if (buf.length() > 0) {
                        out.add(buildChunk(buf.toString()));
                        buf.setLength(0);
                    }
                    out.add(buildChunk(seg.substring(start, end)));
                    start = end;
                }
                continue;
            }
            if (buf.length() > 0 && buf.length() + 1 + seg.length() > targetSize) {
                out.add(buildChunk(buf.toString()));
                buf.setLength(0);
            }
            if (buf.length() > 0) buf.append(' ');
            buf.append(seg);
        }
        if (buf.length() > 0) {
            out.add(buildChunk(buf.toString()));
        }
    }

    private static DocumentChunk buildChunk(String text) {
        DocumentChunk c = new DocumentChunk();
        c.setText(text);
        c.setTokenCount(Integer.valueOf(Math.max(1, text.split("\\s+").length)));
        return c;
    }
}


