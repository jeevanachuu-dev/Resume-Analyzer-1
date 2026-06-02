package com.resumeanalyzer.service;

import java.util.List;

public record ScoreResult(
        double score,
        String label,
        String summary,
        List<String> matchedSkills,
        List<String> missingSkills,
        List<String> suggestions
) {
}
