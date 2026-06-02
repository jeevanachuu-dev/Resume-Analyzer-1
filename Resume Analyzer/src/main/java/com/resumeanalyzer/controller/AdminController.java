package com.resumeanalyzer.controller;

import com.resumeanalyzer.dto.AdminLoginRequest;
import com.resumeanalyzer.dto.AdminLoginResponse;
import com.resumeanalyzer.dto.ApplicationResponse;
import com.resumeanalyzer.dto.RegisteredUserResponse;
import com.resumeanalyzer.dto.ShortlistRequest;
import com.resumeanalyzer.dto.StatsResponse;
import com.resumeanalyzer.service.AdminAuthService;
import com.resumeanalyzer.service.ApplicationService;
import com.resumeanalyzer.service.ResumeDownload;
import com.resumeanalyzer.service.UserAccountService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminAuthService adminAuthService;
    private final ApplicationService applicationService;
    private final UserAccountService userAccountService;

    public AdminController(
            AdminAuthService adminAuthService,
            ApplicationService applicationService,
            UserAccountService userAccountService
    ) {
        this.adminAuthService = adminAuthService;
        this.applicationService = applicationService;
        this.userAccountService = userAccountService;
    }

    @PostMapping("/login")
    public AdminLoginResponse login(@RequestBody AdminLoginRequest request) {
        return adminAuthService.login(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        adminAuthService.logout(adminAuthService.extractToken(request));
    }

    @GetMapping("/applications")
    public List<ApplicationResponse> applications(
            @RequestParam(defaultValue = "rank") String sort,
            @RequestParam(defaultValue = "false") boolean shortlisted
    ) {
        return applicationService.getApplications(sort, shortlisted);
    }

    @PatchMapping("/applications/{id}/shortlist")
    public ApplicationResponse updateShortlist(@PathVariable Long id, @RequestBody ShortlistRequest request) {
        return applicationService.updateShortlist(id, request.shortlisted());
    }

    @GetMapping("/applications/{id}/resume")
    public ResponseEntity<Resource> downloadResume(@PathVariable Long id) {
        ResumeDownload download = applicationService.getResumeDownload(id);
        String contentType = download.contentType() == null || download.contentType().isBlank()
                ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                : download.contentType();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + download.originalFileName() + "\"")
                .body(download.resource());
    }

    @GetMapping("/stats")
    public StatsResponse stats() {
        return applicationService.getStats();
    }

    @GetMapping("/users")
    public List<RegisteredUserResponse> registeredUsers() {
        return userAccountService.registeredUsers();
    }
}
