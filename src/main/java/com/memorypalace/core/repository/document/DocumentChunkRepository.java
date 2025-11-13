package com.memorypalace.core.repository.document;

import com.memorypalace.core.model.DocumentChunk;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, UUID> {
    @Query("select c from DocumentChunk c where c.version.id = :versionId order by c.ord asc")
    List<DocumentChunk> findByVersionOrdered(@Param("versionId") UUID versionId);
}


