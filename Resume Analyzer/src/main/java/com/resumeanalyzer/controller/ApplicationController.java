package com.resumeanalyzer.controller;

import com.resumeanalyzer.dto.ApplicationResponse;
import com.resumeanalyzer.dto.JobComparisonResponse;
import com.resumeanalyzer.dto.RecruiterDashboardResponse;
import com.resumeanalyzer.dto.StatsResponse;
import com.resumeanalyzer.service.ApplicationService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse apply(
            @RequestParam @NotBlank String fullName,
            @RequestParam @NotBlank @Email String email,
            @RequestParam @NotBlank String phone,
            @RequestParam @NotNull Long jobId,
            @RequestParam @NotBlank String skills,
            @RequestParam(defaultValue = "0") int experienceYears,
            @RequestParam(defaultValue = "") String education,
            @RequestParam MultipartFile resume
    ) {
        return applicationService.apply(fullName, email, phone, jobId, skills, experienceYears, education, resume);
    }

    @PostMapping(value = "/compare", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<JobComparisonResponse> compareResumeWithJobs(
            @RequestParam @NotBlank String skills,
            @RequestParam(defaultValue = "0") int experienceYears,
            @RequestParam(defaultValue = "") String education,
            @RequestParam(required = false) List<Long> jobIds,
            @RequestParam MultipartFile resume
    ) {
        return applicationService.compareResumeWithJobs(skills, experienceYears, education, resume, jobIds);
    }

    @GetMapping("/{id}")
    public ApplicationResponse getApplication(@PathVariable Long id) {
        return applicationService.getApplication(id);
    }

    @GetMapping("/stats")
    public StatsResponse stats() {
        return applicationService.getStats();
    }

    @GetMapping("/dashboard")
    public RecruiterDashboardResponse dashboard() {
        return applicationService.getRecruiterDashboard();
    }
}
