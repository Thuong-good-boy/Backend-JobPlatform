package com.jobplatform.job_recruitment_system.dtos.request;

import lombok.Data;

import java.util.List;
@Data
public class CandidateSearchRequest {
    private String keyword;
    private List<String> skills;
    private String location;
    private Integer minExperience;
}

