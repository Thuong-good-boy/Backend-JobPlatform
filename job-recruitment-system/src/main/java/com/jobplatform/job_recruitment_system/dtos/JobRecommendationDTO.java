package com.jobplatform.job_recruitment_system.dtos;

import com.jobplatform.job_recruitment_system.models.Job;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobRecommendationDTO {
    private Job job;
    private Double matchScore;
    private String matchDetails;
}