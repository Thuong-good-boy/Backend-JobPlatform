package com.jobplatform.job_recruitment_system.dtos;

import lombok.Data;

@Data
public class ApplyRequest {
    private Long jobId;
    private Long cvId; // ID của hồ sơ trong bảng CVs
    private String coverLetter;
}
