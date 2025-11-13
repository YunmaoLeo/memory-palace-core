package com.memorypalace.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "document_chunk",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_version_ord", columnNames = {"version_id", "ord"})
    }
)
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "version_id", nullable = false)
    private DocumentVersion version;

    @Column(name = "ord", nullable = false)
    private int ord;

    @Enumerated(EnumType.STRING)
    @Column(name = "modality", length = 16)
    private Modality modality = Modality.TEXT;

    @Column(name = "section_title", length = 500)
    private String sectionTitle;

    @Column(name = "page_start")
    private Integer pageStart;

    @Column(name = "page_end")
    private Integer pageEnd;

    @Column(name = "bbox_json", columnDefinition = "text")
    private String bboxJson;

    @Column(name = "text", nullable = false, columnDefinition = "text")
    private String text;

    @Column(name = "token_count")
    private Integer tokenCount;

    @Column(name = "text_sha256", length = 64)
    private String textSha256;

    @Column(name = "ocr_text", columnDefinition = "text")
    private String ocrText;

    @Column(name = "caption", columnDefinition = "text")
    private String caption;

    @Column(name = "alt_text", length = 500)
    private String altText;

    // LLM enrichments
    @Column(name = "summary", columnDefinition = "text")
    private String summary;

    @Column(name = "keywords", length = 1000)
    private String keywords; // comma-separated to keep it simple

    @Column(name = "auto_section_title", length = 500)
    private String autoSectionTitle;

    @Column(name = "category", length = 120)
    private String category;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public DocumentVersion getVersion() { return version; }
    public void setVersion(DocumentVersion version) { this.version = version; }
    public int getOrd() { return ord; }
    public void setOrd(int ord) { this.ord = ord; }
    public Modality getModality() { return modality; }
    public void setModality(Modality modality) { this.modality = modality; }
    public String getSectionTitle() { return sectionTitle; }
    public void setSectionTitle(String sectionTitle) { this.sectionTitle = sectionTitle; }
    public Integer getPageStart() { return pageStart; }
    public void setPageStart(Integer pageStart) { this.pageStart = pageStart; }
    public Integer getPageEnd() { return pageEnd; }
    public void setPageEnd(Integer pageEnd) { this.pageEnd = pageEnd; }
    public String getBboxJson() { return bboxJson; }
    public void setBboxJson(String bboxJson) { this.bboxJson = bboxJson; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public Integer getTokenCount() { return tokenCount; }
    public void setTokenCount(Integer tokenCount) { this.tokenCount = tokenCount; }
    public String getTextSha256() { return textSha256; }
    public void setTextSha256(String textSha256) { this.textSha256 = textSha256; }
    public String getOcrText() { return ocrText; }
    public void setOcrText(String ocrText) { this.ocrText = ocrText; }
    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }
    public String getAltText() { return altText; }
    public void setAltText(String altText) { this.altText = altText; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }
    public String getAutoSectionTitle() { return autoSectionTitle; }
    public void setAutoSectionTitle(String autoSectionTitle) { this.autoSectionTitle = autoSectionTitle; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Instant getCreatedAt() { return createdAt; }
}


