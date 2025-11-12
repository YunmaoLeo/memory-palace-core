package com.memorypalace.core.service;

import com.memorypalace.core.model.AppUser;
import com.memorypalace.core.model.Tenant;
import com.memorypalace.core.repository.TenantRepository;
import com.memorypalace.core.repository.UserRepository;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, TenantRepository tenantRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public String register(String email, String password, String displayName) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }
        Tenant tenant = tenantRepository.save(new Tenant());
        AppUser user = new AppUser();
        user.setTenant(tenant);
        user.setEmail(email);
        user.setDisplayName(displayName);
        user.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));
        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Email already registered");
        }
        return jwtService.generateToken(user);
    }

    @Transactional(readOnly = true)
    public String login(String email, String password) {
        Optional<AppUser> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        AppUser user = userOpt.get();
        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        return jwtService.generateToken(user);
    }
}


