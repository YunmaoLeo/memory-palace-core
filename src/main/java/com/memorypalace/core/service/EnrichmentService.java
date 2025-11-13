package com.memorypalace.core.service;

import com.memorypalace.core.service.enrich.EnrichmentRequest;
import com.memorypalace.core.service.enrich.EnrichmentResponse;

public interface EnrichmentService {
    EnrichmentResponse enrich(EnrichmentRequest request);
}


