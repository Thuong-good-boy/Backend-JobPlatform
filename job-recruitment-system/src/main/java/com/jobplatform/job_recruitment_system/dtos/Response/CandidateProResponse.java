package com.jobplatform.job_recruitment_system.dtos.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CandidateProResponse {
    private  Long id;
    private  Integer durationDays;
    private BigDecimal price;
    private  String name;
    private  Integer pointsGranted;
}
