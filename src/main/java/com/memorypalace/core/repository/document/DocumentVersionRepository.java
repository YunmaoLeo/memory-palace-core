package com.memorypalace.core.repository.document;

import com.memorypalace.core.model.DocumentVersion;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, UUID> {
    @Query("select v from DocumentVersion v where v.document.id = :docId order by v.versionNum desc")
    Optional<DocumentVersion> findTopByDocumentOrderByVersionDesc(@Param("docId") UUID documentId);
}


