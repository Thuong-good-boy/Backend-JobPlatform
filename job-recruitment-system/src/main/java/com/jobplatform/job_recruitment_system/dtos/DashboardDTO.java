package com.jobplatform.job_recruitment_system.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class DashboardDTO {
    private long totalApplied;
    private long totalSaved;
    private int averageMatchScore;
    private List<JobResponse> suggestedJobs; // Top 3-5 job match nhất
}
