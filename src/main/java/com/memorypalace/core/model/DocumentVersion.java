package com.memorypalace.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "document_version",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_doc_version", columnNames = {"document_id", "version_num"})
    }
)
public class DocumentVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @ManyToOne(optional = false)
    @JoinColumn(name = "stored_file_id", nullable = false)
    private StoredFile storedFile;

    @Column(name = "version_num", nullable = false)
    private int versionNum;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private DocumentStatus status = DocumentStatus.UPLOADED;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public Document getDocument() { return document; }
    public void setDocument(Document document) { this.document = document; }
    public StoredFile getStoredFile() { return storedFile; }
    public void setStoredFile(StoredFile storedFile) { this.storedFile = storedFile; }
    public int getVersionNum() { return versionNum; }
    public void setVersionNum(int versionNum) { this.versionNum = versionNum; }
    public DocumentStatus getStatus() { return status; }
    public void setStatus(DocumentStatus status) { this.status = status; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}


