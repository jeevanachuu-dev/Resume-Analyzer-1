package com.resumeanalyzer.controller;

import com.resumeanalyzer.dto.UserLoginRequest;
import com.resumeanalyzer.dto.UserRegisterRequest;
import com.resumeanalyzer.dto.UserSessionResponse;
import com.resumeanalyzer.service.UserAccountService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserAccountService userAccountService;

    public UserController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserSessionResponse register(@RequestBody UserRegisterRequest request) {
        return userAccountService.register(request);
    }

    @PostMapping("/login")
    public UserSessionResponse login(@RequestBody UserLoginRequest request) {
        return userAccountService.login(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        userAccountService.logout(userAccountService.extractToken(request));
    }

    @GetMapping("/me")
    public UserSessionResponse currentUser(HttpServletRequest request) {
        return userAccountService.currentUser(userAccountService.extractToken(request));
    }
}
