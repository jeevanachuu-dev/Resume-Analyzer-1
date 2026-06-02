package com.resumeanalyzer.service;

import com.resumeanalyzer.dto.RegisteredUserResponse;
import com.resumeanalyzer.dto.UserLoginRequest;
import com.resumeanalyzer.dto.UserRegisterRequest;
import com.resumeanalyzer.dto.UserSessionResponse;
import com.resumeanalyzer.entity.RegisteredUser;
import com.resumeanalyzer.repository.RegisteredUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class UserAccountService {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final Duration TOKEN_TTL = Duration.ofHours(8);
    private static final String HASH_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int HASH_ITERATIONS = 65_000;
    private static final int HASH_LENGTH = 256;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final RegisteredUserRepository registeredUserRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, UserSession> sessions = new ConcurrentHashMap<>();

    public UserAccountService(RegisteredUserRepository registeredUserRepository) {
        this.registeredUserRepository = registeredUserRepository;
    }

    @Transactional
    public UserSessionResponse register(UserRegisterRequest request) {
        String fullName = clean(request == null ? "" : request.fullName());
        String email = clean(request == null ? "" : request.email()).toLowerCase();
        String username = clean(request == null ? "" : request.username());
        String password = request == null ? "" : safe(request.password());

        validateRegistration(fullName, email, username, password);

        if (registeredUserRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered.");
        }

        if (registeredUserRepository.existsByUsernameIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken.");
        }

        RegisteredUser user = new RegisteredUser();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setUsername(username);
        user.setPasswordHash(hashPassword(password));
        user.setLastLoginAt(LocalDateTime.now());
        user.setLoginCount(1);

        return createSession(registeredUserRepository.save(user));
    }

    @Transactional
    public UserSessionResponse login(UserLoginRequest request) {
        String usernameOrEmail = clean(request == null ? "" : request.username());
        String password = request == null ? "" : safe(request.password());

        if (usernameOrEmail.isBlank() || password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username/email and password are required.");
        }

        RegisteredUser user = findByUsernameOrEmail(usernameOrEmail);
        if (!verifyPassword(password, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid user login details.");
        }

        user.setLastLoginAt(LocalDateTime.now());
        user.setLoginCount(user.getLoginCount() + 1);
        return createSession(user);
    }

    public void logout(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }

    @Transactional(readOnly = true)
    public UserSessionResponse currentUser(String token) {
        UserSession session = requireSession(token);
        RegisteredUser user = registeredUserRepository.findById(session.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User session is no longer valid."));
        return toSessionResponse(token, user, session.expiresAt());
    }

    @Transactional(readOnly = true)
    public List<RegisteredUserResponse> registeredUsers() {
        removeExpiredSessions();
        Set<Long> loggedInUserIds = sessions.values().stream()
                .map(UserSession::userId)
                .collect(Collectors.toSet());

        return registeredUserRepository.findAll(Sort.by(Sort.Direction.DESC, "registeredAt"))
                .stream()
                .map(user -> new RegisteredUserResponse(
                        user.getId(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getUsername(),
                        user.getRegisteredAt(),
                        user.getLastLoginAt(),
                        user.getLoginCount(),
                        loggedInUserIds.contains(user.getId())
                ))
                .toList();
    }

    public String extractToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
            return authorization.substring(BEARER_PREFIX.length()).trim();
        }
        return request.getHeader("X-User-Token");
    }

    private void validateRegistration(String fullName, String email, String username, String password) {
        if (fullName.isBlank() || email.isBlank() || username.isBlank() || password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Full name, email, username and password are required.");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a valid email address.");
        }

        if (username.length() < 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username must be at least 3 characters.");
        }

        if (password.length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 6 characters.");
        }
    }

    private RegisteredUser findByUsernameOrEmail(String usernameOrEmail) {
        return registeredUserRepository.findByUsernameIgnoreCase(usernameOrEmail)
                .or(() -> registeredUserRepository.findByEmailIgnoreCase(usernameOrEmail))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid user login details."));
    }

    private UserSessionResponse createSession(RegisteredUser user) {
        removeExpiredSessions();
        String token = UUID.randomUUID() + "-" + UUID.randomUUID();
        Instant expiresAt = Instant.now().plus(TOKEN_TTL);
        sessions.put(token, new UserSession(user.getId(), expiresAt));
        return toSessionResponse(token, user, expiresAt);
    }

    private UserSession requireSession(String token) {
        removeExpiredSessions();
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User login is required.");
        }

        UserSession session = sessions.get(token);
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User session expired. Please login again.");
        }
        return session;
    }

    private void removeExpiredSessions() {
        Instant now = Instant.now();
        sessions.entrySet().removeIf(entry -> now.isAfter(entry.getValue().expiresAt()));
    }

    private UserSessionResponse toSessionResponse(String token, RegisteredUser user, Instant expiresAt) {
        return new UserSessionResponse(
                token,
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getRegisteredAt(),
                expiresAt
        );
    }

    private String hashPassword(String password) {
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        byte[] hash = pbkdf2(password.toCharArray(), salt);
        return HASH_ALGORITHM + ":" + HASH_ITERATIONS + ":" + Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    }

    private boolean verifyPassword(String password, String storedHash) {
        if (storedHash == null || storedHash.isBlank()) {
            return false;
        }

        String[] parts = storedHash.split(":");
        if (parts.length != 4 || !HASH_ALGORITHM.equals(parts[0])) {
            return false;
        }

        byte[] salt = Base64.getDecoder().decode(parts[2]);
        byte[] expected = Base64.getDecoder().decode(parts[3]);
        byte[] actual = pbkdf2(password.toCharArray(), salt);
        return MessageDigest.isEqual(expected, actual);
    }

    private byte[] pbkdf2(char[] password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, HASH_ITERATIONS, HASH_LENGTH);
            return SecretKeyFactory.getInstance(HASH_ALGORITHM).generateSecret(spec).getEncoded();
        } catch (Exception exception) {
            throw new IllegalStateException("Could not process password securely.", exception);
        }
    }

    private String clean(String value) {
        return safe(value).trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private record UserSession(Long userId, Instant expiresAt) {
    }
}
