package com.resumeanalyzer.dto;

import java.time.Instant;

public record AdminLoginResponse(
        String token,
        String username,
        Instant expiresAt
) {
}
