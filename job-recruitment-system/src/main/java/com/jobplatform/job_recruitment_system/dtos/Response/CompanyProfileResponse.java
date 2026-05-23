package com.jobplatform.job_recruitment_system.dtos.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyProfileResponse{
    private String companyName;
    private String taxCode;
    private String website;
    private String address;
    private String description;
    private String logoUrl;
    private boolean isVerified;
    private String email;
    private Integer remainingBoosts;
    private  Integer remainingCvViews;
    private LocalDateTime proEnd;

}
