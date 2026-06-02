package com.resumeanalyzer;

import com.resumeanalyzer.dto.AdminLoginRequest;
import com.resumeanalyzer.dto.AdminLoginResponse;
import com.resumeanalyzer.repository.JobPostingRepository;
import com.resumeanalyzer.service.AdminAuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ResumeAnalyzerApplicationTests {

    @Autowired
    private AdminAuthService adminAuthService;

    @Autowired
    private JobPostingRepository jobPostingRepository;

    @Test
    void contextStartsWithSeedDataAndDefaultAdminLogin() {
        AdminLoginResponse login = adminAuthService.login(new AdminLoginRequest("admin", "admin123"));

        assertThat(login.token()).isNotBlank();
        assertThat(login.username()).isEqualTo("admin");
        assertThat(jobPostingRepository.count()).isGreaterThanOrEqualTo(7);
    }
}
