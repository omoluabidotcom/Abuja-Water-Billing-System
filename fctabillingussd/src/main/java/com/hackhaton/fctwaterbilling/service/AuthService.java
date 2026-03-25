package com.hackhaton.fctwaterbilling.service;

import com.hackhaton.fctwaterbilling.entity.SystemUser;
import com.hackhaton.fctwaterbilling.enums.UserRole;
import com.hackhaton.fctwaterbilling.enums.UserStatus;
import com.hackhaton.fctwaterbilling.exception.AuthException;
import com.hackhaton.fctwaterbilling.repository.SystemUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SystemUserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // ── Login ──────────────────────────────────────────────────────────

    /**
     * Authenticates a user by username and raw password.
     * Updates {@code lastLoginAt} on success.
     *
     * @throws AuthException with a user-friendly message on any failure
     */
    @Transactional
    public SystemUser login(String username, String rawPassword) {
        SystemUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException("Invalid username or password."));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new AuthException("Invalid username or password.");
        }

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new AuthException("This account is inactive. Please contact an administrator.");
        }

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new AuthException("This account has been suspended. Please contact an administrator.");
        }

        user.setLastLoginAt(OffsetDateTime.now());
        userRepository.save(user);
        return user;
    }

    // ── Register (admin-facing, creates any role) ──────────────────────

    /**
     * Creates a new system user. Passwords are BCrypt-hashed before persistence.
     *
     * @throws AuthException if the username is already taken
     */
    @Transactional
    public SystemUser createUser(String username, String rawPassword,
                                  UserRole role, String createdBy) {
        if (userRepository.existsByUsername(username)) {
            throw new AuthException("Username '" + username + "' is already taken.");
        }

        SystemUser user = SystemUser.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role(role)
                .status(UserStatus.ACTIVE)
                .createdBy(createdBy)
                .build();

        return userRepository.save(user);
    }

    // ── Password change ────────────────────────────────────────────────

    @Transactional
    public void changePassword(String username, String currentRaw, String newRaw) {
        SystemUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException("User not found."));

        if (!passwordEncoder.matches(currentRaw, user.getPasswordHash())) {
            throw new AuthException("Current password is incorrect.");
        }

        user.setPasswordHash(passwordEncoder.encode(newRaw));
        userRepository.save(user);
    }
}

