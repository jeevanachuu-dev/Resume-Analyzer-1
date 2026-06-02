package com.resumeanalyzer.dto;

import java.util.List;

public record JobComparisonResponse(
        int rank,
        JobResponse job,
        double score,
        String scoreLabel,
        String scoreSummary,
        List<String> matchedSkills,
        List<String> missingSkills,
        List<String> suggestions
) {
}
