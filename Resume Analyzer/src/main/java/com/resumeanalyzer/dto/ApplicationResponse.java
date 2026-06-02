package com.resumeanalyzer.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ApplicationResponse(
        Long id,
        int rank,
        String fullName,
        String email,
        String phone,
        JobResponse job,
        String skills,
        int experienceYears,
        String education,
        double score,
        String scoreLabel,
        String scoreSummary,
        List<String> matchedSkills,
        List<String> missingSkills,
        List<String> suggestions,
        boolean shortlisted,
        String status,
        String resumeFileName,
        long resumeSize,
        String resumeDownloadUrl,
        LocalDateTime appliedAt
) {
}
