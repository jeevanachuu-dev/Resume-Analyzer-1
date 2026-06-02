package com.resumeanalyzer.service;

import com.resumeanalyzer.dto.AdminLoginRequest;
import com.resumeanalyzer.dto.AdminLoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AdminAuthService {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final Duration TOKEN_TTL = Duration.ofHours(8);

    private final String adminUsername;
    private final String adminPassword;
    private final Map<String, Instant> sessions = new ConcurrentHashMap<>();

    public AdminAuthService(
            @Value("${app.admin.username}") String adminUsername,
            @Value("${app.admin.password}") String adminPassword
    ) {
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    public AdminLoginResponse login(AdminLoginRequest request) {
        String username = request == null ? "" : request.username();
        String password = request == null ? "" : request.password();

        if (!matches(adminUsername, username) || !matches(adminPassword, password)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin username or password.");
        }

        String token = UUID.randomUUID() + "-" + UUID.randomUUID();
        Instant expiresAt = Instant.now().plus(TOKEN_TTL);
        sessions.put(token, expiresAt);
        removeExpiredSessions();

        return new AdminLoginResponse(token, adminUsername, expiresAt);
    }

    public void logout(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }

    public boolean isValidToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        Instant expiresAt = sessions.get(token);
        if (expiresAt == null) {
            return false;
        }

        if (Instant.now().isAfter(expiresAt)) {
            sessions.remove(token);
            return false;
        }

        return true;
    }

    public String extractToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
            return authorization.substring(BEARER_PREFIX.length()).trim();
        }
        return request.getHeader("X-Admin-Token");
    }

    private void removeExpiredSessions() {
        Instant now = Instant.now();
        sessions.entrySet().removeIf(entry -> now.isAfter(entry.getValue()));
    }

    private boolean matches(String expected, String actual) {
        byte[] expectedBytes = safe(expected).getBytes(StandardCharsets.UTF_8);
        byte[] actualBytes = safe(actual).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expectedBytes, actualBytes);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
