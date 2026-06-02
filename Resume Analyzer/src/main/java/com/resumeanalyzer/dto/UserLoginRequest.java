package com.resumeanalyzer.dto;

public record UserLoginRequest(
        String username,
        String password
) {
}
