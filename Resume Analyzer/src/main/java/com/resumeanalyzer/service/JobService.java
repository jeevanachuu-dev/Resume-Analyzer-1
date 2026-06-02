package com.resumeanalyzer.service;

import com.resumeanalyzer.dto.JobResponse;
import com.resumeanalyzer.repository.JobPostingRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class JobService {

    private final JobPostingRepository jobPostingRepository;

    public JobService(JobPostingRepository jobPostingRepository) {
        this.jobPostingRepository = jobPostingRepository;
    }

    @Transactional(readOnly = true)
    public List<JobResponse> getJobs() {
        return jobPostingRepository.findAll(Sort.by(Sort.Direction.ASC, "title"))
                .stream()
                .map(job -> new JobResponse(
                        job.getId(),
                        job.getTitle(),
                        job.getCompany(),
                        job.getLocation(),
                        job.getDescription(),
                        job.getRequiredSkills(),
                        job.getMinExperience()
                ))
                .toList();
    }
}
