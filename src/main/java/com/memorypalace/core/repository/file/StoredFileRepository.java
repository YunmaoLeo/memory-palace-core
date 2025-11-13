package com.memorypalace.core.repository.file;

import com.memorypalace.core.model.StoredFile;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoredFileRepository extends JpaRepository<StoredFile, UUID> {

    @Query("select f from StoredFile f where f.tenant.id = :tenantId and f.sha256Hex = :sha256")
    Optional<StoredFile> findByTenantAndSha256(@Param("tenantId") UUID tenantId, @Param("sha256") String sha256Hex);

    @Query("""
        select f from StoredFile f
        where f.tenant.id = :tenantId
          and (:q is null or lower(f.originalFilename) like lower(concat('%', :q, '%')))
          and (:projectId is null or (f.folder is not null and f.folder.project.id = :projectId))
          and (:folderId is null or (f.folder is not null and f.folder.id = :folderId))
        order by f.createdAt desc
        """)
    java.util.List<StoredFile> searchByName(
        @Param("tenantId") UUID tenantId,
        @Param("q") String q,
        @Param("projectId") UUID projectId,
        @Param("folderId") UUID folderId
    );
}


