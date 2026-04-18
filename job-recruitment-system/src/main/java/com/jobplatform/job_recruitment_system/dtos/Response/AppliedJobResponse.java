package com.jobplatform.job_recruitment_system.dtos.Response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppliedJobResponse {

    private Long id;
    private Long jobId;

    private String jobTitle;
    private String companyName;
    private String logoUrl;
    private String appliedDate;
    private String location;
    private String salary;

    private String status;
}