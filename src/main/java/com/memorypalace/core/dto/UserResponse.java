package com.memorypalace.core.dto;

import java.util.UUID;

public class UserResponse {
    private UUID id;
    private UUID tenantId;
    private String email;
    private String displayName;

    public UserResponse(UUID id, UUID tenantId, String email, String displayName) {
        this.id = id;
        this.tenantId = tenantId;
        this.email = email;
        this.displayName = displayName;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public String getEmail() { return email; }
    public String getDisplayName() { return displayName; }
}


