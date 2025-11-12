package com.memorypalace.core.repository;

import com.memorypalace.core.model.StoredFile;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoredFileRepository extends JpaRepository<StoredFile, UUID> {

    @Query("select f from StoredFile f where f.tenant.id = :tenantId and f.sha256Hex = :sha256")
    Optional<StoredFile> findByTenantAndSha256(@Param("tenantId") UUID tenantId, @Param("sha256") String sha256Hex);
}


