package com.memorypalace.core.dto;

import java.time.Instant;
import java.util.UUID;

public class FileUploadResponse {
    private UUID id;
    private UUID tenantId;
    private UUID ownerId;
    private String title;
    private String originalFilename;
    private String mimeType;
    private long sizeBytes;
    private String sha256Hex;
    private String s3Bucket;
    private String s3Key;
    private Instant createdAt;
    private boolean duplicate;

    public FileUploadResponse(UUID id, UUID tenantId, UUID ownerId, String title, String originalFilename,
                              String mimeType, long sizeBytes, String sha256Hex, String s3Bucket, String s3Key,
                              Instant createdAt, boolean duplicate) {
        this.id = id;
        this.tenantId = tenantId;
        this.ownerId = ownerId;
        this.title = title;
        this.originalFilename = originalFilename;
        this.mimeType = mimeType;
        this.sizeBytes = sizeBytes;
        this.sha256Hex = sha256Hex;
        this.s3Bucket = s3Bucket;
        this.s3Key = s3Key;
        this.createdAt = createdAt;
        this.duplicate = duplicate;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getOwnerId() { return ownerId; }
    public String getTitle() { return title; }
    public String getOriginalFilename() { return originalFilename; }
    public String getMimeType() { return mimeType; }
    public long getSizeBytes() { return sizeBytes; }
    public String getSha256Hex() { return sha256Hex; }
    public String getS3Bucket() { return s3Bucket; }
    public String getS3Key() { return s3Key; }
    public Instant getCreatedAt() { return createdAt; }
    public boolean isDuplicate() { return duplicate; }
}


