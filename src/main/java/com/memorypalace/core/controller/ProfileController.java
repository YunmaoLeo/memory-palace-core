package com.memorypalace.core.controller;

import com.memorypalace.core.dto.UserResponse;
import com.memorypalace.core.model.AppUser;
import com.memorypalace.core.repository.user.UserRepository;
import com.memorypalace.core.security.TenantContext;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<?> me() {
        var principal = TenantContext.get();
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(java.util.Map.of("error", "Unauthorized"));
        }
        UUID userId = principal.userId;
        AppUser user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(java.util.Map.of("error", "Unauthorized"));
        }
        return ResponseEntity.ok(new UserResponse(user.getId(), user.getTenant().getId(), user.getEmail(), user.getDisplayName()));
    }
}


