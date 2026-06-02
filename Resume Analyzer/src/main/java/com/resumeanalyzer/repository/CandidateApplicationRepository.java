package com.resumeanalyzer.repository;

import com.resumeanalyzer.entity.CandidateApplication;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CandidateApplicationRepository extends JpaRepository<CandidateApplication, Long> {

    List<CandidateApplication> findAllByShortlistedTrue(Sort sort);

    long countByShortlistedTrue();

    @Query("select coalesce(avg(application.score), 0) from CandidateApplication application")
    double averageScore();
}
