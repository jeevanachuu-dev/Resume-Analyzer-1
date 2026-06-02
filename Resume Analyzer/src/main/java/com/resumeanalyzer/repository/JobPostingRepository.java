package com.resumeanalyzer.repository;

import com.resumeanalyzer.entity.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    boolean existsByTitleAndCompany(String title, String company);
}
