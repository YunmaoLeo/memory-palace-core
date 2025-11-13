package com.memorypalace.core.service.enrich;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("openai-enrich")
public class OpenAIEnrichmentClient implements com.memorypalace.core.service.EnrichmentService {

    private final String model;

    public OpenAIEnrichmentClient(
        @Value("${enrich.provider.model:gpt-4o-mini}") String model
    ) {
        this.model = model;
    }

    @Override
    public EnrichmentResponse enrich(EnrichmentRequest request) {
        // Placeholder: protocol definition only, actual HTTP call intentionally omitted.
        // Expected request body (Chat Completions style) would include:
        // - system prompt describing the required JSON schema
        // - user content with the chunk text
        // The response would be parsed into EnrichmentResponse.
        throw new UnsupportedOperationException("OpenAI client not wired yet. Use 'mock-enrich' profile for now.");
    }

    public String getModel() {
        return model;
    }
}


