package com.memorypalace.core.model;

public enum DocumentStatus {
    UPLOADED,       // StoredFile is present; not parsed yet
    PARSING,        // Extracting raw text into chunks
    ENRICHING,      // LLM-assisted enrichment (summaries, keywords, titles)
    READY,          // Chunks saved and enriched
    FAILED          // Pipeline failed (see errorMessage on version)
}


