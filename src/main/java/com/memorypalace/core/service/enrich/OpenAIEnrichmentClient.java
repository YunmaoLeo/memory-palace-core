package com.memorypalace.core.service.enrich;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.memorypalace.core.config.OpenAIClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("openai-enrich")
public class OpenAIEnrichmentClient implements com.memorypalace.core.service.EnrichmentService {

    private final String model;
    private final OpenAIClient client;

    public OpenAIEnrichmentClient(
        OpenAIClient client,
        @Value("${openai.models.chat:gpt-4o-mini}") String model
    ) {
        this.client = client;
        this.model = model;
    }

    @Override
    public EnrichmentResponse enrich(EnrichmentRequest request) {
        if (!client.isConfigured()) {
            throw new IllegalStateException("OpenAI API key not configured");
        }
        ObjectNode body = client.mapper().createObjectNode();
        body.put("model", model);
        // Force JSON object outputs when supported
        ObjectNode respFormat = client.mapper().createObjectNode();
        respFormat.put("type", "json_object");
        body.set("response_format", respFormat);

        ObjectNode sys = client.mapper().createObjectNode();
        sys.put("role", "system");
        sys.put("content",
            "You are an assistant that extracts structured enrichment data for document chunks. "
                + "Output strictly a JSON object with fields: "
                + "summary (string), keywords (array of strings), autoSectionTitle (string), category (string). "
                + "Keep summary concise (2-3 sentences)."
        );
        ObjectNode user = client.mapper().createObjectNode();
        user.put("role", "user");
        String lang = request.getLanguage() != null ? request.getLanguage() : "";
        user.put("content", (lang.isEmpty() ? "" : ("Language: " + lang + "\n")) + "Chunk:\n" + request.getText());

        body.set("messages", client.mapper().createArrayNode().add(sys).add(user));

        JsonNode resp = client.post("/chat/completions", body);
        String content = resp.path("choices").path(0).path("message").path("content").asText("");
        if (content.isEmpty()) {
            throw new RuntimeException("Empty enrichment response");
        }
        try {
            JsonNode obj = client.mapper().readTree(content);
            String summary = obj.path("summary").asText(null);
            String autoTitle = obj.path("autoSectionTitle").asText(null);
            String category = obj.path("category").asText(null);
            java.util.List<String> keywords = new java.util.ArrayList<>();
            JsonNode ks = obj.path("keywords");
            if (ks.isArray()) {
                ks.forEach(k -> keywords.add(k.asText()));
            }
            return new EnrichmentResponse(summary, keywords, autoTitle, category);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse enrichment JSON: " + e.getMessage(), e);
        }
    }
}


