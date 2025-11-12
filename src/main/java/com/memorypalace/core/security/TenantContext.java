package com.memorypalace.core.security;

import java.util.UUID;

public final class TenantContext {
    public static final class Principal {
        public final UUID userId;
        public final UUID tenantId;
        public final String email;
        public Principal(UUID userId, UUID tenantId, String email) {
            this.userId = userId;
            this.tenantId = tenantId;
            this.email = email;
        }
    }

    private static final ThreadLocal<Principal> CURRENT = new ThreadLocal<>();

    private TenantContext() {}

    public static void set(Principal principal) {
        CURRENT.set(principal);
    }

    public static Principal get() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }
}


