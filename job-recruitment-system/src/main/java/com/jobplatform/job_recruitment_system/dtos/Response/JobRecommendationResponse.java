package com.jobplatform.job_recruitment_system.dtos.Response;

import com.jobplatform.job_recruitment_system.models.Job;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobRecommendationResponse {
    private Job job;
    private Double matchScore;
    private String matchDetails;
    private String nameCompany;
}