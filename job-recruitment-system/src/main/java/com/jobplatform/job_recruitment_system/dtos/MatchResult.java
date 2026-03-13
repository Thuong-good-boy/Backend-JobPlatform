package com.jobplatform.job_recruitment_system.dtos;

import lombok.Data;

@Data
public class MatchResult {
    private Double score;
    private String reason;
}
