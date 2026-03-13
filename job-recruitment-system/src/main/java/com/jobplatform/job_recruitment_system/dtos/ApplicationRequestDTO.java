package com.jobplatform.job_recruitment_system.dtos;
import lombok.Data;

@Data
public class ApplicationRequestDTO {
    private Long jobId;
    private Long userId;
    private Long cvId;
    private String fullname;
    private String phone;
    private String address;
    private String coverLetter;
}