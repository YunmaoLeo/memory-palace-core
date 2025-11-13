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
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chunk_asset")
public class ChunkAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "version_id", nullable = false)
    private DocumentVersion version;

    @Column(name = "s3_bucket", nullable = false, length = 200)
    private String s3Bucket;

    @Column(name = "s3_key", nullable = false, length = 1024)
    private String s3Key;

    @Column(name = "mime_type", length = 200, nullable = false)
    private String mimeType;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "page_index")
    private Integer pageIndex;

    @Column(name = "bbox_json", columnDefinition = "text")
    private String bboxJson;

    @Column(name = "sha256_hex", length = 64)
    private String sha256Hex;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public DocumentVersion getVersion() { return version; }
    public void setVersion(DocumentVersion version) { this.version = version; }
    public String getS3Bucket() { return s3Bucket; }
    public void setS3Bucket(String s3Bucket) { this.s3Bucket = s3Bucket; }
    public String getS3Key() { return s3Key; }
    public void setS3Key(String s3Key) { this.s3Key = s3Key; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public Integer getWidth() { return width; }
    public void setWidth(Integer width) { this.width = width; }
    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }
    public Integer getPageIndex() { return pageIndex; }
    public void setPageIndex(Integer pageIndex) { this.pageIndex = pageIndex; }
    public String getBboxJson() { return bboxJson; }
    public void setBboxJson(String bboxJson) { this.bboxJson = bboxJson; }
    public String getSha256Hex() { return sha256Hex; }
    public void setSha256Hex(String sha256Hex) { this.sha256Hex = sha256Hex; }
    public Instant getCreatedAt() { return createdAt; }
}


