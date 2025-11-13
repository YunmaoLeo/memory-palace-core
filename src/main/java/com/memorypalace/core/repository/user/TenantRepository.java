package com.memorypalace.core.repository.user;

import com.memorypalace.core.model.Tenant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
}


