package com.memorypalace.core.service.enrich;

public class EnrichmentRequest {
    private String text;
    private String language; // optional, e.g. "en", "zh"

    public EnrichmentRequest() {}
    public EnrichmentRequest(String text, String language) {
        this.text = text;
        this.language = language;
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
}


