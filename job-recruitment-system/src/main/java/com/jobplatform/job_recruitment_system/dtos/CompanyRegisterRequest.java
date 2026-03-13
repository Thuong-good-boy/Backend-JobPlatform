package com.jobplatform.job_recruitment_system.dtos;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CompanyRegisterRequest {
    private String email;
    private String password;
    private String fullName;

    private String companyName;
    private String address;
    private String website;
    private String description;

    private MultipartFile licenseImage;
}