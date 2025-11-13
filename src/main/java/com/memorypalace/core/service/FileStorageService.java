package com.memorypalace.core.service;

import com.memorypalace.core.model.AppUser;
import com.memorypalace.core.model.StoredFile;
import com.memorypalace.core.repository.file.StoredFileRepository;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class FileStorageService {

    private final S3Client s3Client;
    private final StoredFileRepository storedFileRepository;
    private final String bucketName;

    public FileStorageService(S3Client s3Client, StoredFileRepository storedFileRepository,
                              @Value("${storage.s3.bucket}") String bucketName) {
        this.s3Client = s3Client;
        this.storedFileRepository = storedFileRepository;
        this.bucketName = bucketName;
        ensureBucketExists();
    }

    public static final class UploadResult {
        public final StoredFile storedFile;
        public final boolean duplicate;
        public UploadResult(StoredFile storedFile, boolean duplicate) {
            this.storedFile = storedFile;
            this.duplicate = duplicate;
        }
    }

    public UploadResult upload(AppUser owner, MultipartFile file, String title) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Empty file");
        }
        // Write to temp file to compute sha256 and reuse for upload
        Path tmp = Files.createTempFile("upload-", ".bin");
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, tmp, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }

        String sha256 = computeSha256Hex(tmp);
        UUID tenantId = owner.getTenant().getId();

        Optional<StoredFile> existing = storedFileRepository.findByTenantAndSha256(tenantId, sha256);
        if (existing.isPresent()) {
            // Duplicate: return existing metadata
            return new UploadResult(existing.get(), true);
        }

        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String safeName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        LocalDate today = LocalDate.now();
        String key = "tenant/" + tenantId + "/" + today.getYear() + "/" + String.format("%02d", today.getMonthValue())
            + "/" + UUID.randomUUID() + "_" + safeName;

        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        long size = Files.size(tmp);

        PutObjectRequest putReq = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .contentType(contentType)
            .build();
        try (InputStream uploadIn = Files.newInputStream(tmp)) {
            s3Client.putObject(putReq, RequestBody.fromInputStream(uploadIn, size));
        }

        StoredFile stored = new StoredFile();
        stored.setTenant(owner.getTenant());
        stored.setOwner(owner);
        stored.setTitle(title != null ? title : originalName);
        stored.setOriginalFilename(originalName);
        stored.setMimeType(contentType);
        stored.setSizeBytes(size);
        stored.setSha256Hex(sha256);
        stored.setS3Bucket(bucketName);
        stored.setS3Key(key);

        stored = storedFileRepository.save(stored);
        return new UploadResult(stored, false);
    }

    private void ensureBucketExists() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
        } catch (NoSuchBucketException e) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
        } catch (Exception ignore) {
            // If permissions prevent headBucket, attempt create; ignore if already exists
            try {
                s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
            } catch (Exception ignored) {
                // best-effort
            }
        }
    }

    private static String computeSha256Hex(Path file) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (DigestInputStream dis = new DigestInputStream(Files.newInputStream(file), md)) {
                byte[] buffer = new byte[8192];
                while (dis.read(buffer) != -1) {
                    // consume stream
                }
            }
            return HexFormat.of().formatHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}


