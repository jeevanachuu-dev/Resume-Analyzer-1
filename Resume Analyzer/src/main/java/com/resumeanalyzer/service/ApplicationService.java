package com.resumeanalyzer.service;

import com.resumeanalyzer.dto.ApplicationResponse;
import com.resumeanalyzer.dto.DailyApplicationResponse;
import com.resumeanalyzer.dto.JobComparisonResponse;
import com.resumeanalyzer.dto.JobResponse;
import com.resumeanalyzer.dto.RecruiterDashboardResponse;
import com.resumeanalyzer.dto.SkillStatResponse;
import com.resumeanalyzer.dto.StatsResponse;
import com.resumeanalyzer.entity.CandidateApplication;
import com.resumeanalyzer.entity.JobPosting;
import com.resumeanalyzer.repository.CandidateApplicationRepository;
import com.resumeanalyzer.repository.JobPostingRepository;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ApplicationService {

    private static final Sort RANKING_SORT = Sort.by(Sort.Direction.DESC, "score")
            .and(Sort.by(Sort.Direction.DESC, "appliedAt"))
            .and(Sort.by(Sort.Direction.ASC, "id"));

    private final CandidateApplicationRepository applicationRepository;
    private final JobPostingRepository jobPostingRepository;
    private final ResumeStorageService resumeStorageService;
    private final ResumeScoringService resumeScoringService;

    public ApplicationService(
            CandidateApplicationRepository applicationRepository,
            JobPostingRepository jobPostingRepository,
            ResumeStorageService resumeStorageService,
            ResumeScoringService resumeScoringService
    ) {
        this.applicationRepository = applicationRepository;
        this.jobPostingRepository = jobPostingRepository;
        this.resumeStorageService = resumeStorageService;
        this.resumeScoringService = resumeScoringService;
    }

    @Transactional
    public ApplicationResponse apply(
            String fullName,
            String email,
            String phone,
            Long jobId,
            String skills,
            int experienceYears,
            String education,
            MultipartFile resume
    ) {
        JobPosting jobPosting = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Selected job was not found."));

        String resumeText = resumeStorageService.extractReadableText(resume);
        StoredResume storedResume = resumeStorageService.store(resume);
        ScoreResult scoreResult = resumeScoringService.score(jobPosting, skills, experienceYears, education, resumeText);

        CandidateApplication application = new CandidateApplication();
        application.setFullName(fullName.trim());
        application.setEmail(email.trim());
        application.setPhone(phone.trim());
        application.setJobPosting(jobPosting);
        application.setSkills(skills.trim());
        application.setExperienceYears(Math.max(experienceYears, 0));
        application.setEducation(education == null ? "" : education.trim());
        application.setScore(scoreResult.score());
        application.setScoreLabel(scoreResult.label());
        application.setScoreSummary(scoreResult.summary());
        application.setMatchedSkills(String.join(", ", scoreResult.matchedSkills()));
        application.setMissingSkills(String.join(", ", scoreResult.missingSkills()));
        application.setResumeSuggestions(String.join("\n", scoreResult.suggestions()));
        application.setShortlisted(false);
        application.setStatus("Applied");
        application.setResumeOriginalName(storedResume.originalName());
        application.setResumeStoredName(storedResume.storedName());
        application.setResumeContentType(storedResume.contentType());
        application.setResumeSize(storedResume.size());

        return toResponse(applicationRepository.save(application));
    }

    @Transactional(readOnly = true)
    public List<JobComparisonResponse> compareResumeWithJobs(
            String skills,
            int experienceYears,
            String education,
            MultipartFile resume,
            List<Long> jobIds
    ) {
        resumeStorageService.validateResumeFile(resume);

        List<Long> requestedJobIds = jobIds == null
                ? List.of()
                : jobIds.stream().filter(Objects::nonNull).distinct().toList();

        List<JobPosting> jobs = requestedJobIds.isEmpty()
                ? jobPostingRepository.findAll(Sort.by(Sort.Direction.ASC, "title"))
                : jobPostingRepository.findAllById(requestedJobIds);

        if (!requestedJobIds.isEmpty() && jobs.size() != requestedJobIds.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "One or more selected jobs were not found.");
        }

        if (jobs.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No jobs are available for comparison.");
        }

        String resumeText = resumeStorageService.extractReadableText(resume);
        List<JobScore> scoredJobs = jobs.stream()
                .map(job -> new JobScore(job, resumeScoringService.score(
                        job,
                        skills == null ? "" : skills,
                        Math.max(experienceYears, 0),
                        education == null ? "" : education,
                        resumeText
                )))
                .sorted(Comparator
                        .comparingDouble((JobScore jobScore) -> jobScore.scoreResult().score())
                        .reversed()
                        .thenComparing(jobScore -> jobScore.jobPosting().getTitle()))
                .toList();

        List<JobComparisonResponse> responses = new ArrayList<>();
        for (int index = 0; index < scoredJobs.size(); index++) {
            JobScore jobScore = scoredJobs.get(index);
            responses.add(toComparisonResponse(index + 1, jobScore.jobPosting(), jobScore.scoreResult()));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getApplication(Long id) {
        return applicationRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application was not found."));
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplications(String sortBy, boolean shortlistedOnly) {
        Sort sort = "score".equalsIgnoreCase(sortBy) || "rank".equalsIgnoreCase(sortBy)
                ? RANKING_SORT
                : Sort.by(Sort.Direction.DESC, "appliedAt");

        List<CandidateApplication> applications = shortlistedOnly
                ? applicationRepository.findAllByShortlistedTrue(sort)
                : applicationRepository.findAll(sort);
        Map<Long, Integer> ranks = candidateRanks();

        return applications.stream()
                .map(application -> toResponse(application, ranks.getOrDefault(application.getId(), 0)))
                .toList();
    }

    @Transactional
    public ApplicationResponse updateShortlist(Long id, boolean shortlisted) {
        CandidateApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application was not found."));

        application.setShortlisted(shortlisted);
        application.setStatus(shortlisted ? "Shortlisted" : "Applied");
        return toResponse(application);
    }

    @Transactional(readOnly = true)
    public ResumeDownload getResumeDownload(Long id) {
        CandidateApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application was not found."));

        Resource resource = resumeStorageService.load(application.getResumeStoredName());
        return new ResumeDownload(resource, application.getResumeOriginalName(), application.getResumeContentType());
    }

    @Transactional(readOnly = true)
    public StatsResponse getStats() {
        long total = applicationRepository.count();
        long shortlisted = applicationRepository.countByShortlistedTrue();
        double averageScore = Math.round(applicationRepository.averageScore() * 10.0) / 10.0;
        return new StatsResponse(total, shortlisted, averageScore, total);
    }

    @Transactional(readOnly = true)
    public RecruiterDashboardResponse getRecruiterDashboard() {
        List<CandidateApplication> applications = applicationRepository.findAll();
        LocalDate today = LocalDate.now();
        long total = applications.size();
        long shortlisted = applications.stream().filter(CandidateApplication::isShortlisted).count();
        double averageScore = total == 0
                ? 0
                : Math.round(applications.stream().mapToDouble(CandidateApplication::getScore).average().orElse(0) * 10.0) / 10.0;
        long todayApplications = applications.stream()
                .filter(application -> application.getAppliedAt() != null)
                .filter(application -> application.getAppliedAt().toLocalDate().isEqual(today))
                .count();

        return new RecruiterDashboardResponse(
                total,
                shortlisted,
                averageScore,
                todayApplications,
                topSkills(applications),
                dailyApplications(applications, today)
        );
    }

    private ApplicationResponse toResponse(CandidateApplication application) {
        return toResponse(application, rankOf(application));
    }

    private ApplicationResponse toResponse(CandidateApplication application, int rank) {
        return new ApplicationResponse(
                application.getId(),
                rank,
                application.getFullName(),
                application.getEmail(),
                application.getPhone(),
                toJobResponse(application.getJobPosting()),
                application.getSkills(),
                application.getExperienceYears(),
                application.getEducation(),
                application.getScore(),
                application.getScoreLabel(),
                application.getScoreSummary(),
                splitCsv(application.getMatchedSkills()),
                splitCsv(application.getMissingSkills()),
                splitLines(application.getResumeSuggestions()),
                application.isShortlisted(),
                application.getStatus(),
                application.getResumeOriginalName(),
                application.getResumeSize(),
                "/api/admin/applications/" + application.getId() + "/resume",
                application.getAppliedAt()
        );
    }

    private JobComparisonResponse toComparisonResponse(int rank, JobPosting jobPosting, ScoreResult scoreResult) {
        return new JobComparisonResponse(
                rank,
                toJobResponse(jobPosting),
                scoreResult.score(),
                scoreResult.label(),
                scoreResult.summary(),
                scoreResult.matchedSkills(),
                scoreResult.missingSkills(),
                scoreResult.suggestions()
        );
    }

    private JobResponse toJobResponse(JobPosting jobPosting) {
        return new JobResponse(
                jobPosting.getId(),
                jobPosting.getTitle(),
                jobPosting.getCompany(),
                jobPosting.getLocation(),
                jobPosting.getDescription(),
                jobPosting.getRequiredSkills(),
                jobPosting.getMinExperience()
        );
    }

    private List<String> splitCsv(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .toList();
    }

    private List<String> splitLines(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split("\\R"))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .toList();
    }

    private int rankOf(CandidateApplication application) {
        if (application.getId() == null) {
            return 0;
        }
        return candidateRanks().getOrDefault(application.getId(), 0);
    }

    private Map<Long, Integer> candidateRanks() {
        List<CandidateApplication> rankedApplications = applicationRepository.findAll(RANKING_SORT);
        Map<Long, Integer> ranks = new HashMap<>();
        for (int index = 0; index < rankedApplications.size(); index++) {
            ranks.put(rankedApplications.get(index).getId(), index + 1);
        }
        return ranks;
    }

    private List<SkillStatResponse> topSkills(List<CandidateApplication> applications) {
        Map<String, Long> skillCounts = new HashMap<>();
        applications.stream()
                .flatMap(application -> splitSkillText(application.getSkills()).stream())
                .map(this::normalizeSkill)
                .filter(skill -> !skill.isBlank())
                .forEach(skill -> skillCounts.merge(skill, 1L, Long::sum));

        return skillCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(8)
                .map(entry -> new SkillStatResponse(displaySkill(entry.getKey()), entry.getValue()))
                .toList();
    }

    private List<String> splitSkillText(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }

        String normalized = value
                .replaceAll("(?i)\\s+and\\s+", ",")
                .replaceAll("[;\\n\\r/|]+", ",");

        return Arrays.stream(normalized.split(","))
                .map(String::trim)
                .filter(skill -> !skill.isBlank())
                .toList();
    }

    private List<DailyApplicationResponse> dailyApplications(List<CandidateApplication> applications, LocalDate today) {
        Map<LocalDate, Long> countsByDate = applications.stream()
                .filter(application -> application.getAppliedAt() != null)
                .collect(Collectors.groupingBy(
                        application -> application.getAppliedAt().toLocalDate(),
                        Collectors.counting()
                ));

        List<DailyApplicationResponse> dailyApplications = new ArrayList<>();
        for (int daysAgo = 6; daysAgo >= 0; daysAgo--) {
            LocalDate date = today.minusDays(daysAgo);
            dailyApplications.add(new DailyApplicationResponse(date, countsByDate.getOrDefault(date, 0L)));
        }
        return dailyApplications;
    }

    private String normalizeSkill(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9+#.\\s-]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String displaySkill(String normalizedSkill) {
        return Arrays.stream(normalizedSkill.split("\\s+"))
                .filter(word -> !word.isBlank())
                .map(word -> word.length() <= 3 ? word.toUpperCase(Locale.ROOT) : Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }

    private record JobScore(JobPosting jobPosting, ScoreResult scoreResult) {
    }
}
