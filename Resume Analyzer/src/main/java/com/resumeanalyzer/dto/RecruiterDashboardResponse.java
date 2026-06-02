package com.resumeanalyzer.dto;

import java.util.List;

public record RecruiterDashboardResponse(
        long totalApplications,
        long shortlistedApplications,
        double averageResumeScore,
        long todayApplications,
        List<SkillStatResponse> topSkills,
        List<DailyApplicationResponse> dailyApplications
) {
}
