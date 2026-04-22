package com.jobplatform.job_recruitment_system.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApplyRequest {
    @NotBlank(message = "JOB_REQUIRED")
    private Long jobId;
    @NotBlank(message = "CV_REQUIRED")
    private Long cvId;
    private String coverLetter;
}
