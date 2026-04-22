package com.jobplatform.job_recruitment_system.dtos.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SummaryReponse {
    private Long totalActiveJobs;
    private Long totalApplications;
    private Long totalSavedJobs;
}
