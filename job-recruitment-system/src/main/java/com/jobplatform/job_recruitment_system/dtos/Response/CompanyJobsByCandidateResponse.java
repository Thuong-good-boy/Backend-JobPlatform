package com.jobplatform.job_recruitment_system.dtos.Response;

import java.time.LocalDateTime;

public interface CompanyJobsByCandidateResponse {
    Long getId();
    String getTitle();
    String getLocation();
    Integer getSalaryMin();
    Integer getSalaryMax();
    String getStatus();
    LocalDateTime getCreatedAt();

    String[] getSkills();

    Integer getTotalApplications();
    Integer getNewApplications();

    Double getMatchScore();
    String getMatchDetails();
}