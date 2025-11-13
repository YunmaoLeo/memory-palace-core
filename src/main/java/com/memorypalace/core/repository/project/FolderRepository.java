package com.memorypalace.core.repository.project;

import com.memorypalace.core.model.Folder;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FolderRepository extends JpaRepository<Folder, UUID> {
    @Query("select f from Folder f where f.project.id = :projectId and f.tenant.id = :tenantId order by f.createdAt desc")
    List<Folder> findByProject(@Param("projectId") UUID projectId, @Param("tenantId") UUID tenantId);
}


