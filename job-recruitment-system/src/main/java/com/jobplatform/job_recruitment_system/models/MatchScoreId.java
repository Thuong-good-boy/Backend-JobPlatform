package com.jobplatform.job_recruitment_system.models;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchScoreId implements Serializable {
    private Long jobId;
    private Long cvId;
}