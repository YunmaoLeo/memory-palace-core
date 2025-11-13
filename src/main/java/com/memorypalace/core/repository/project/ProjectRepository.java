package com.memorypalace.core.repository.project;

import com.memorypalace.core.model.Project;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    @Query("select p from Project p where p.tenant.id = :tenantId order by p.createdAt desc")
    List<Project> findByTenant(@Param("tenantId") UUID tenantId);
}


