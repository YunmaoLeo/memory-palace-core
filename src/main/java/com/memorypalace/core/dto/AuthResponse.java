package com.memorypalace.core.dto;

import java.util.UUID;

public class AuthResponse {
    private String token;
    private UUID userId;
    private UUID tenantId;
    private String email;

    public AuthResponse(String token, UUID userId, UUID tenantId, String email) {
        this.token = token;
        this.userId = userId;
        this.tenantId = tenantId;
        this.email = email;
    }

    public String getToken() { return token; }
    public UUID getUserId() { return userId; }
    public UUID getTenantId() { return tenantId; }
    public String getEmail() { return email; }
}


