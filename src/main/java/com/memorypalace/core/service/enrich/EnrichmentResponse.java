package com.memorypalace.core.service.enrich;

import java.util.List;

public class EnrichmentResponse {
    private String summary;
    private List<String> keywords;
    private String autoSectionTitle;
    private String category;

    public EnrichmentResponse() {}

    public EnrichmentResponse(String summary, List<String> keywords, String autoSectionTitle, String category) {
        this.summary = summary;
        this.keywords = keywords;
        this.autoSectionTitle = autoSectionTitle;
        this.category = category;
    }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }
    public String getAutoSectionTitle() { return autoSectionTitle; }
    public void setAutoSectionTitle(String autoSectionTitle) { this.autoSectionTitle = autoSectionTitle; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}


