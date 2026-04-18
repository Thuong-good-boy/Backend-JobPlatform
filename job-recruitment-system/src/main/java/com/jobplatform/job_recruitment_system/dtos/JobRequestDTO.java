package com.jobplatform.job_recruitment_system.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class JobRequestDTO {
    private String title;
    private String description;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String location;
    private Set<Long> skillIds;
}
