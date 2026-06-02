package com.resumeanalyzer.config;

import com.resumeanalyzer.entity.JobPosting;
import com.resumeanalyzer.repository.JobPostingRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final JobPostingRepository jobPostingRepository;

    public DataSeeder(JobPostingRepository jobPostingRepository) {
        this.jobPostingRepository = jobPostingRepository;
    }

    @Override
    public void run(String... args) {
        List<JobPosting> seedJobs = List.of(
                new JobPosting(
                        "Java Spring Boot Developer",
                        "TalentSync Labs",
                        "Bengaluru",
                        "Build REST APIs, connect MySQL data models and improve hiring automation products.",
                        "Java, Spring Boot, REST API, MySQL, Hibernate, Git",
                        2
                ),
                new JobPosting(
                        "Frontend Developer",
                        "PixelHire Studio",
                        "Remote",
                        "Create responsive candidate dashboards, animated workflows and admin screens.",
                        "HTML, CSS, JavaScript, Responsive Design, API Integration, UI Animation",
                        1
                ),
                new JobPosting(
                        "Data Analyst",
                        "InsightWorks",
                        "Hyderabad",
                        "Analyze hiring data, prepare dashboards and score candidate trends for recruiters.",
                        "SQL, Excel, Power BI, Python, Statistics, Data Visualization",
                        1
                ),
                new JobPosting(
                        "HR Executive",
                        "PeopleCore",
                        "Chennai",
                        "Manage applications, shortlist candidates and coordinate interview pipelines.",
                        "Recruitment, Screening, Communication, ATS, Interview Coordination",
                        0
                ),
                new JobPosting(
                        "Fresher Software Trainee",
                        "TalentSync Labs",
                        "Bengaluru",
                        "Entry-level role for graduates to work on Java, web applications and database-backed features.",
                        "Java, HTML, CSS, JavaScript, SQL, Git, Problem Solving",
                        0
                ),
                new JobPosting(
                        "Junior QA Tester - Fresher",
                        "PixelHire Studio",
                        "Remote",
                        "Test web applications, write test cases, report bugs and support release quality.",
                        "Manual Testing, Test Cases, Bug Reporting, SDLC, SQL, Communication",
                        0
                ),
                new JobPosting(
                        "Graduate Data Analyst",
                        "InsightWorks",
                        "Hyderabad",
                        "Fresher-friendly data role focused on Excel reports, SQL analysis and hiring dashboards.",
                        "Excel, SQL, Power BI, Python, Statistics, Data Cleaning",
                        0
                ),
                new JobPosting(
                        "AI Prompt Engineering Intern",
                        "NovaAI Solutions",
                        "Remote",
                        "Create, test and improve AI prompts for resume screening, chatbot workflows and content automation.",
                        "Prompt Engineering, Generative AI, ChatGPT, Communication, Research, Python",
                        0
                ),
                new JobPosting(
                        "Cloud Support Associate - Fresher",
                        "CloudCore Services",
                        "Pune",
                        "Support cloud tickets, monitor services and help teams troubleshoot basic deployment issues.",
                        "Cloud Computing, AWS, Azure, Linux, Networking, Troubleshooting",
                        0
                ),
                new JobPosting(
                        "Cybersecurity Analyst Trainee",
                        "SecureNet Digital",
                        "Bengaluru",
                        "Monitor security alerts, document incidents and learn vulnerability assessment workflows.",
                        "Cybersecurity, Network Security, Linux, SIEM, Vulnerability Assessment, Communication",
                        0
                ),
                new JobPosting(
                        "DevOps Engineer Trainee",
                        "DeployFlow Technologies",
                        "Hyderabad",
                        "Assist with CI/CD pipelines, deployment scripts, container basics and cloud infrastructure monitoring.",
                        "Git, Linux, Docker, CI/CD, Jenkins, Cloud, Shell Scripting",
                        0
                ),
                new JobPosting(
                        "Full Stack Developer - Fresher",
                        "CodeSprint Labs",
                        "Chennai",
                        "Build web application features using frontend screens, backend APIs and database integration.",
                        "Java, Spring Boot, JavaScript, HTML, CSS, REST API, MySQL",
                        0
                ),
                new JobPosting(
                        "Python Developer Intern",
                        "DataBridge Analytics",
                        "Remote",
                        "Develop Python scripts, automate reports and support backend data processing tasks.",
                        "Python, SQL, APIs, Git, Problem Solving, Data Processing",
                        0
                ),
                new JobPosting(
                        "QA Automation Tester - Fresher",
                        "QualityWorks Studio",
                        "Coimbatore",
                        "Write test cases, perform manual testing and learn automation testing for web applications.",
                        "Manual Testing, Selenium, Java, Test Cases, Bug Reporting, SDLC",
                        0
                ),
                new JobPosting(
                        "Data Engineer Trainee",
                        "PipelineIQ Systems",
                        "Bengaluru",
                        "Support data pipelines, clean datasets and prepare SQL-based data processing jobs.",
                        "SQL, Python, ETL, Data Cleaning, Database, Excel",
                        0
                ),
                new JobPosting(
                        "Business Analyst Trainee",
                        "FinEdge Systems",
                        "Mumbai",
                        "Gather requirements, prepare process documents and support product teams with data-backed decisions.",
                        "Business Analysis, SQL, Excel, Documentation, Communication, Requirement Gathering",
                        0
                ),
                new JobPosting(
                        "UI/UX Designer - Fresher",
                        "PixelCraft Product Studio",
                        "Remote",
                        "Design wireframes, improve user flows and create clean interfaces for web and mobile products.",
                        "Figma, UI Design, UX Research, Wireframes, Prototyping, HTML, CSS",
                        0
                ),
                new JobPosting(
                        "Technical Support Engineer - Fresher",
                        "HelpDeskPro Technologies",
                        "Noida",
                        "Handle customer technical issues, analyze logs and coordinate bug reports with engineering teams.",
                        "Technical Support, SQL, Linux, Troubleshooting, Communication, Ticketing Tools",
                        0
                ),
                new JobPosting(
                        "Machine Learning Intern",
                        "SmartVision AI",
                        "Bengaluru",
                        "Work on basic ML experiments, model evaluation and data preparation for AI product features.",
                        "Python, Machine Learning, Pandas, NumPy, Data Preprocessing, Statistics",
                        0
                )
        );

        seedJobs.stream()
                .filter(job -> !jobPostingRepository.existsByTitleAndCompany(job.getTitle(), job.getCompany()))
                .forEach(jobPostingRepository::save);
    }
}
