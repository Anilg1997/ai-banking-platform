package com.banking.auth.controller;

import com.banking.auth.dto.AuthResponse;
import com.banking.auth.model.User;
import com.banking.auth.repository.UserRepository;
import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final UserRepository userRepository;

    @GetMapping("")
    public ResponseEntity<List<AuthResponse.UserInfo>> listUsers() {
        List<AuthResponse.UserInfo> users = userRepository.findAll().stream()
                .map(u -> AuthResponse.UserInfo.builder()
                        .id(u.getId())
                        .username(u.getUsername())
                        .email(u.getEmail())
                        .firstName(u.getFirstName())
                        .lastName(u.getLastName())
                        .roles(u.getRoles().stream().map(Enum::name).collect(Collectors.toSet()))
                        .emailVerified(u.isEmailVerified())
                        .twoFactorEnabled(u.isTwoFactorEnabled())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthResponse.UserInfo> getUser(@PathVariable String id) {
        return userRepository.findById(id)
                .map(u -> AuthResponse.UserInfo.builder()
                        .id(u.getId())
                        .username(u.getUsername())
                        .email(u.getEmail())
                        .firstName(u.getFirstName())
                        .lastName(u.getLastName())
                        .roles(u.getRoles().stream().map(Enum::name).collect(Collectors.toSet()))
                        .emailVerified(u.isEmailVerified())
                        .twoFactorEnabled(u.isTwoFactorEnabled())
                        .build())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<Void> updateUserRole(@PathVariable String id, @RequestBody Map<String, String> body) {
        String role = body.get("role");
        if (role == null) return ResponseEntity.badRequest().build();
        return userRepository.findById(id).map(u -> {
            u.getRoles().clear();
            u.getRoles().add(com.banking.auth.model.Role.valueOf(role));
            userRepository.save(u);
            return ResponseEntity.<Void>ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> toggleUserStatus(@PathVariable String id, @RequestBody Map<String, Boolean> body) {
        boolean active = body.getOrDefault("active", true);
        return userRepository.findById(id).map(u -> {
            u.setAccountNonLocked(active);
            userRepository.save(u);
            return ResponseEntity.<Void>ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        List<User> allUsers = userRepository.findAll();
        long totalUsers = allUsers.size();
        long activeUsers = allUsers.stream().filter(User::isAccountNonLocked).count();
        long verifiedUsers = allUsers.stream().filter(User::isEmailVerified).count();
        return ResponseEntity.ok(Map.of(
                "totalUsers", totalUsers,
                "activeUsers", activeUsers,
                "verifiedUsers", verifiedUsers,
                "lockedUsers", totalUsers - activeUsers
        ));
    }
}
