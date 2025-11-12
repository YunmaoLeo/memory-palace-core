package com.memorypalace.core.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.memorypalace.core.service.JwtService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class JwtAuthFilter implements Filter {

    private static final Set<String> PUBLIC_PATH_PREFIXES = Set.of(
        "/actuator", "/api/auth"
    );

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = req.getRequestURI();
        boolean isPublic = PUBLIC_PATH_PREFIXES.stream().anyMatch(path::startsWith);

        try {
            String auth = req.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) {
                String token = auth.substring("Bearer ".length());
                DecodedJWT jwt = jwtService.verify(token);
                UUID userId = jwtService.getUserId(jwt);
                UUID tenantId = jwtService.getTenantId(jwt);
                String email = jwtService.getEmail(jwt);
                TenantContext.set(new TenantContext.Principal(userId, tenantId, email));
            } else if (!isPublic) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.setContentType("application/json");
                resp.getWriter().write("{\"error\":\"Missing Authorization header\"}");
                return;
            }

            chain.doFilter(request, response);
        } catch (Exception ex) {
            if (!isPublic) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.setContentType("application/json");
                resp.getWriter().write("{\"error\":\"Invalid token\"}");
                return;
            }
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}


