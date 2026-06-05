package com.banking.auth.service;

import com.banking.auth.dto.AuthResponse;
import com.banking.auth.dto.LoginRequest;
import com.banking.auth.dto.RegisterRequest;
import com.banking.auth.model.Role;
import com.banking.auth.model.User;
import com.banking.auth.repository.UserRepository;
import com.banking.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;

    @Value("${app.security.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${app.security.lock-duration-minutes:15}")
    private int lockDurationMinutes;

    @Value("${app.jwt.expiration:3600000}")
    private long jwtExpiration;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .roles(Set.of(Role.ROLE_USER))
                .emailVerified(true) // Auto-verified for local dev
                .build();

        user = userRepository.save(user);
        log.info("New user registered: {}", user.getUsername());

        String accessToken = jwtTokenProvider.generateToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        // Store refresh token in Redis
        redisTemplate.opsForValue().set(
                "refresh_token:" + user.getId(),
                refreshToken,
                jwtTokenProvider.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS
        );

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid username/email or password"));

        // Check if account is locked
        if (!user.isAccountNonLocked()) {
            if (user.getLockTime() != null) {
                if (LocalDateTime.now().isAfter(user.getLockTime().plusMinutes(lockDurationMinutes))) {
                    unlockAccount(user);
                } else {
                    throw new LockedException("Account is locked. Please try again later.");
                }
            }
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword())
            );

            // Reset failed attempts on successful login
            userRepository.updateFailedAttempts(0, user.getUsername());
            userRepository.updateLastLogin(LocalDateTime.now(), user.getId());

            String accessToken = jwtTokenProvider.generateToken(user);
            String refreshToken = jwtTokenProvider.generateRefreshToken(user);

            // Store refresh token in Redis
            redisTemplate.opsForValue().set(
                    "refresh_token:" + user.getId(),
                    refreshToken,
                    jwtTokenProvider.getRefreshTokenExpiration(),
                    TimeUnit.MILLISECONDS
            );

            log.info("User logged in: {}", user.getUsername());
            return buildAuthResponse(user, accessToken, refreshToken);

        } catch (BadCredentialsException e) {
            handleFailedLogin(user);
            throw new BadCredentialsException("Invalid username/email or password");
        }
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        String storedToken = redisTemplate.opsForValue().get("refresh_token:" + userId);

        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new IllegalArgumentException("Refresh token has been revoked or expired");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Delete old refresh token
        redisTemplate.delete("refresh_token:" + userId);

        String newAccessToken = jwtTokenProvider.generateToken(user);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);

        // Store new refresh token
        redisTemplate.opsForValue().set(
                "refresh_token:" + user.getId(),
                newRefreshToken,
                jwtTokenProvider.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS
        );

        return buildAuthResponse(user, newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String userId) {
        redisTemplate.delete("refresh_token:" + userId);
        log.info("User logged out: {}", userId);
    }

    public void validateToken(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new IllegalArgumentException("Invalid or expired token");
        }
    }

    private void handleFailedLogin(User user) {
        int attempts = user.getFailedAttempts() + 1;
        userRepository.updateFailedAttempts(attempts, user.getUsername());

        if (attempts >= maxFailedAttempts) {
            userRepository.updateLockTime(LocalDateTime.now(), user.getUsername());
            log.warn("Account locked due to too many failed attempts: {}", user.getUsername());
        }
    }

    private void unlockAccount(User user) {
        userRepository.updateFailedAttempts(0, user.getUsername());
        userRepository.updateLockTime(null, user.getUsername());
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .roles(user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()))
                        .emailVerified(user.isEmailVerified())
                        .twoFactorEnabled(user.isTwoFactorEnabled())
                        .build())
                .build();
    }
}
