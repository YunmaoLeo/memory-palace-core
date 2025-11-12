package com.memorypalace.core.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.memorypalace.core.model.AppUser;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final long ttlSeconds;

    public JwtService(
        @Value("${security.jwt.secret:dev-secret-change}") String secret,
        @Value("${security.jwt.ttlSeconds:43200}") long ttlSeconds
    ) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.verifier = JWT.require(algorithm).withIssuer("memory-palace-core").build();
        this.ttlSeconds = ttlSeconds;
    }

    public String generateToken(AppUser user) {
        Instant now = Instant.now();
        return JWT.create()
            .withIssuer("memory-palace-core")
            .withIssuedAt(now)
            .withExpiresAt(now.plusSeconds(ttlSeconds))
            .withSubject(user.getId().toString())
            .withClaim("tenantId", user.getTenant().getId().toString())
            .withClaim("email", user.getEmail())
            .sign(algorithm);
    }

    public DecodedJWT verify(String token) throws JWTVerificationException {
        return verifier.verify(token);
    }

    public UUID getUserId(DecodedJWT jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    public UUID getTenantId(DecodedJWT jwt) {
        return UUID.fromString(jwt.getClaim("tenantId").asString());
    }

    public String getEmail(DecodedJWT jwt) {
        return jwt.getClaim("email").asString();
    }
}


