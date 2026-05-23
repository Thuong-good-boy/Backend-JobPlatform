package com.jobplatform.job_recruitment_system.dtos.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class CandidateProfileResponse {
    private String fullName;
    private String email;
    private String avatarUrl;
    private String title;
    private String bio;
    private Integer experienceYears;
    private String location;
    private Integer expectedSalaryMin;
    private Integer expectedSalaryMax;
    private Boolean isPublic;
    private LocalDateTime proEnd;
    private  Integer aiPoints ;
}
