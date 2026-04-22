package com.jobplatform.job_recruitment_system.dtos.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SummarryAdminReponse {
    private  Long  totalCandidate;
   private  Long totalCompanies;
   private  Long totalJobs;
   private  Long totalApplications;
}
