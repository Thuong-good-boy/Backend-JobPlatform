package com.jobplatform.job_recruitment_system.dtos.Response;

import lombok.Data;

@Data
public class MatchResultReponse {
    private Double score;
    private String reason;
}
