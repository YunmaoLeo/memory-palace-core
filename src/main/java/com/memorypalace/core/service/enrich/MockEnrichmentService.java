package com.memorypalace.core.service.enrich;

import java.util.Arrays;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile({"default", "mock-enrich"})
public class MockEnrichmentService implements com.memorypalace.core.service.EnrichmentService {
    @Override
    public EnrichmentResponse enrich(EnrichmentRequest request) {
        String text = request.getText() != null ? request.getText() : "";
        String trimmed = text.length() > 200 ? text.substring(0, 200) + "..." : text;
        return new EnrichmentResponse(
            "Summary: " + trimmed,
            Arrays.asList("keyword1", "keyword2"),
            "Auto Section",
            "general"
        );
    }
}


