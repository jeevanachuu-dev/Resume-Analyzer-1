package com.resumeanalyzer.dto;

public record StatsResponse(
        long totalApplications,
        long shortlistedApplications,
        double averageScore,
        long resumesUploaded
) {
}
