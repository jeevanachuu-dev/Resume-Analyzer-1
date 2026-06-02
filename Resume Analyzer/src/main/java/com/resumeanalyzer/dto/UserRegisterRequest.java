package com.resumeanalyzer.dto;

public record UserRegisterRequest(
        String fullName,
        String email,
        String username,
        String password
) {
}
