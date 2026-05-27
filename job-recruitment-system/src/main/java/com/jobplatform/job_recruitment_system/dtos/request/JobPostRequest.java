package com.jobplatform.job_recruitment_system.dtos.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class JobPostRequest {
    @NotBlank(message = "TITLE_REQUIRED")
    @Size(max = 255, message = "TITLE_TOO_LONG")
    private String title;
    @NotBlank(message = "DESCRIPTION_REQUIRED")
    @Size(max =1000 , message = "DESCRIPTION_TOO_LONG")
    private String description;
    @PositiveOrZero(message = "SALARY_MIN_MUST_BE_POSITIVE_OR_ZERO")
    private BigDecimal salaryMin;
    @PositiveOrZero(message = "SALARY_MIN_MUST_BE_POSITIVE_OR_ZERO")
    private BigDecimal salaryMax;
    @NotBlank(message = "LOCATION_REQUIRED")
    @Size(max = 255, message = "LOCATION_TOO_LONG")
    private String location;
    @NotEmpty(message = "SKILLS_REQUIRED")
    @Size(max = 10, message = "MAX_10_SKILLS_ALLOWED")
    private List<@NotBlank(message = "SKILL_NAME_CANNOT_BE_BLANK")String> skillNames;
    private double weightSkill;
    private double weightExperience;
    private double weightEducation;
}