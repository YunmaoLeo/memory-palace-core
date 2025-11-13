package com.memorypalace.core.service;

import com.memorypalace.core.model.DocumentChunk;
import com.memorypalace.core.model.StoredFile;
import java.util.List;

public interface TextExtractionService {
    List<DocumentChunk> extractChunks(StoredFile storedFile, int approximateChunkSize);
}


