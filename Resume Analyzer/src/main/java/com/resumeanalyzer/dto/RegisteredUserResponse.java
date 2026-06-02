package com.resumeanalyzer.dto;

import java.time.LocalDateTime;

public record RegisteredUserResponse(
        Long id,
        String fullName,
        String email,
        String username,
        LocalDateTime registeredAt,
        LocalDateTime lastLoginAt,
        int loginCount,
        boolean loggedIn
) {
}
