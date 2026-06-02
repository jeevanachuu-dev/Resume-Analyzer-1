package com.resumeanalyzer.service;

import com.resumeanalyzer.entity.JobPosting;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ResumeScoringService {

    public ScoreResult score(JobPosting jobPosting, String skills, int experienceYears, String education, String resumeText) {
        List<String> requiredSkills = splitSkills(jobPosting.getRequiredSkills());
        String searchText = normalize(String.join(" ", safe(skills), safe(education), safe(resumeText)));

        List<String> matchedSkills = requiredSkills.stream()
                .filter(skill -> containsSkill(searchText, skill))
                .toList();

        List<String> missingSkills = requiredSkills.stream()
                .filter(skill -> !matchedSkills.contains(skill))
                .toList();

        double skillScore = requiredSkills.isEmpty() ? 45 : ((double) matchedSkills.size() / requiredSkills.size()) * 50;
        double experienceScore = experienceScore(experienceYears, jobPosting.getMinExperience());
        double educationScore = educationScore(education, resumeText);
        double resumeDepthScore = resumeDepthScore(resumeText);
        double titleKeywordScore = titleKeywordScore(jobPosting.getTitle(), searchText);

        double score = Math.min(100, skillScore + experienceScore + educationScore + resumeDepthScore + titleKeywordScore);
        score = Math.round(score * 10.0) / 10.0;

        return new ScoreResult(
                score,
                label(score),
                buildSummary(score, matchedSkills.size(), requiredSkills.size(), experienceYears, jobPosting.getMinExperience()),
                matchedSkills,
                missingSkills,
                buildSuggestions(jobPosting, missingSkills, experienceYears, education, resumeText, searchText)
        );
    }

    private List<String> splitSkills(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        Set<String> uniqueSkills = new LinkedHashSet<>();
        Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(skill -> !skill.isBlank())
                .forEach(uniqueSkills::add);
        return List.copyOf(uniqueSkills);
    }

    private boolean containsSkill(String searchText, String skill) {
        return searchText.contains(normalize(skill));
    }

    private double experienceScore(int experienceYears, int minimumExperience) {
        if (minimumExperience <= 0) {
            return 18;
        }
        double ratio = (double) Math.max(experienceYears, 0) / minimumExperience;
        return Math.min(20, ratio * 20);
    }

    private double educationScore(String education, String resumeText) {
        String text = normalize(safe(education) + " " + safe(resumeText));
        if (text.contains("master") || text.contains("mca") || text.contains("mba") || text.contains("m.tech")) {
            return 10;
        }
        if (text.contains("bachelor") || text.contains("b.tech") || text.contains("bca") || text.contains("degree")) {
            return 8;
        }
        return text.isBlank() ? 0 : 5;
    }

    private double resumeDepthScore(String resumeText) {
        int length = safe(resumeText).length();
        if (length > 1200) {
            return 10;
        }
        if (length > 400) {
            return 7;
        }
        if (length > 80) {
            return 4;
        }
        return 2;
    }

    private double titleKeywordScore(String jobTitle, String searchText) {
        long matches = Arrays.stream(normalize(jobTitle).split(" "))
                .filter(word -> word.length() > 3)
                .filter(searchText::contains)
                .count();
        return Math.min(10, matches * 3.5);
    }

    private String label(double score) {
        if (score >= 85) {
            return "Excellent match";
        }
        if (score >= 70) {
            return "Strong match";
        }
        if (score >= 55) {
            return "Good potential";
        }
        return "Needs improvement";
    }

    private String buildSummary(double score, int matched, int total, int experienceYears, int minimumExperience) {
        String skillSummary = total == 0
                ? "No job skills configured."
                : matched + " of " + total + " required skills matched.";
        String experienceSummary = minimumExperience <= 0
                ? "Experience requirement is entry level."
                : experienceYears + " years experience against " + minimumExperience + " years required.";
        return "Score " + score + ". " + skillSummary + " " + experienceSummary;
    }

    private List<String> buildSuggestions(
            JobPosting jobPosting,
            List<String> missingSkills,
            int experienceYears,
            String education,
            String resumeText,
            String searchText
    ) {
        List<String> suggestions = new ArrayList<>();

        if (!missingSkills.isEmpty()) {
            suggestions.add("Add evidence for these job skills: " + String.join(", ", missingSkills) + ".");
        }

        int resumeLength = safe(resumeText).length();
        if (resumeLength < 400) {
            suggestions.add("Expand the resume with projects, internship work, responsibilities and measurable outcomes.");
        }

        if (!searchText.contains("project") && !searchText.contains("internship") && !searchText.contains("experience")) {
            suggestions.add("Add a Projects or Internship section so fresher experience is easy for recruiters to verify.");
        }

        if (jobPosting.getMinExperience() > 0 && experienceYears < jobPosting.getMinExperience()) {
            suggestions.add("Highlight relevant internships, freelance work or academic projects to cover the experience gap.");
        }

        if (jobPosting.getMinExperience() == 0 && experienceYears == 0) {
            suggestions.add("For fresher roles, include academic projects, certifications, GitHub links and training details.");
        }

        if (safe(education).isBlank() && !searchText.contains("bachelor") && !searchText.contains("degree")) {
            suggestions.add("Mention education clearly with degree, college, year and important coursework.");
        }

        if (!searchText.contains("github") && !searchText.contains("portfolio") && !searchText.contains("linkedin")) {
            suggestions.add("Add LinkedIn, GitHub or portfolio links to make profile verification easier.");
        }

        if (suggestions.isEmpty()) {
            suggestions.add("Resume is aligned well. Improve it further by adding stronger metrics and role-specific project impact.");
        }

        return List.copyOf(suggestions);
    }

    private String normalize(String value) {
        return safe(value)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9+#.\\s-]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
