package com.resumeanalyzer.dto;

public record JobResponse(
        Long id,
        String title,
        String company,
        String location,
        String description,
        String requiredSkills,
        int minExperience
) {
}
