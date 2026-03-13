package com.jobplatform.job_recruitment_system.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class JobPostRequest {
    private String title;
    private String description;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String location;
    private String jobType;
    private LocalDateTime deadline;
    private List<String> skillNames;
}