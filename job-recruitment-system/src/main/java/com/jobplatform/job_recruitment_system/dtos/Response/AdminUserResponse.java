package com.jobplatform.job_recruitment_system.dtos.Response;

import com.jobplatform.job_recruitment_system.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserResponse {
    private Long id;
    private String fullName;
    private Role role;
    private Boolean active;
    private String authProvider;

    private String companyName;
    private String website;
    private String description;
    private String address;
    private String taxCode;
    private Integer remainingBoosts;
    private Boolean isVerified;

    private String title;
    private String bio;
    private Integer experienceYears;
    private String location;
    private Integer expectedSalaryMin;
    private Integer expectedSalaryMax;
    private Boolean isPublic;
}