package com.resumeanalyzer.dto;

public record AdminLoginRequest(
        String username,
        String password
) {
}
