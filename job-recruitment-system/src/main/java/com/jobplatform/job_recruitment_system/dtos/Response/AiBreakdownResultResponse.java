package com.jobplatform.job_recruitment_system.dtos.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiBreakdownResultResponse {
    private double skillScore;
    private double experienceScore;
    private double educationScore;
    private String reason;
}
