package com.resumeanalyzer.dto;

import java.time.Instant;
import java.time.LocalDateTime;

public record UserSessionResponse(
        String token,
        String username,
        String fullName,
        String email,
        LocalDateTime registeredAt,
        Instant expiresAt
) {
}
