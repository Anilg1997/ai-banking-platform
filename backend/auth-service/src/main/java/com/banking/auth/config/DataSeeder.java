package com.banking.auth.config;

import com.banking.auth.model.Role;
import com.banking.auth.model.User;
import com.banking.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdminUser();
        seedDemoUser();
    }

    private void seedAdminUser() {
        if (userRepository.existsByUsername("admin")) {
            log.info("Admin user already exists, skipping seed");
            return;
        }

        User admin = User.builder()
                .username("admin")
                .email("admin@novabank.com")
                .password(passwordEncoder.encode("Admin@123"))
                .firstName("System")
                .lastName("Administrator")
                .phoneNumber("+1-800-555-0199")
                .roles(Set.of(Role.ROLE_ADMIN, Role.ROLE_USER))
                .emailVerified(true)
                .accountNonLocked(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .twoFactorEnabled(false)
                .failedAttempts(0)
                .build();

        userRepository.save(admin);
        log.info("Seeded admin user: admin / Admin@123");
    }

    private void seedDemoUser() {
        if (userRepository.existsByUsername("demo")) {
            log.info("Demo user already exists, skipping seed");
            return;
        }

        User demo = User.builder()
                .username("demo")
                .email("demo@novabank.com")
                .password(passwordEncoder.encode("Demo@123"))
                .firstName("John")
                .lastName("Demo")
                .phoneNumber("+1-800-555-0100")
                .roles(Set.of(Role.ROLE_USER))
                .emailVerified(true)
                .accountNonLocked(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .twoFactorEnabled(false)
                .failedAttempts(0)
                .build();

        userRepository.save(demo);
        log.info("Seeded demo user: demo / Demo@123");
    }
}