package com.jobplatform.job_recruitment_system.dtos.Response;

import com.jobplatform.job_recruitment_system.dtos.RecentApplicationReponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyDashboardResponse {
    private String companyName;
    private String logoUrl;
    private boolean isVerified;
    private long totalJobs;
    private long activeJobs;
    private long totalApplications;
    private long newApplications;

    @Valid
    private List<RecentApplicationReponse> recentApplications;
}