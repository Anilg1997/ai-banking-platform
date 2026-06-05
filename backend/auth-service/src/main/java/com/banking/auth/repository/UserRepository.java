package com.banking.auth.repository;

import com.banking.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.failedAttempts = :attempts WHERE u.username = :username")
    void updateFailedAttempts(@Param("attempts") int attempts, @Param("username") String username);

    @Modifying
    @Query("UPDATE User u SET u.lockTime = :lockTime WHERE u.username = :username")
    void updateLockTime(@Param("lockTime") LocalDateTime lockTime, @Param("username") String username);

    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :lastLogin WHERE u.id = :userId")
    void updateLastLogin(@Param("lastLogin") LocalDateTime lastLogin, @Param("userId") String userId);
}
