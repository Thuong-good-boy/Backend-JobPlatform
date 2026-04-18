package com.jobplatform.job_recruitment_system.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CompanyOnboardingRequest {
    @NotBlank(message = "COMPANY_NAME_REQUIRED")
    private String companyName;
    @URL(message = "INVALID_WEBSITE_URL")
    private String website;
    @Size(max = 500 , message = "DESCRIPTION_TOO_LONG")
    private String description;
    @NotBlank(message = "ADDRESS_REQUIRED")
    @Size(max = 255, message = "ADDRESS_TOO_LONG")
    private String address;
    @NotBlank(message = "TAX_CODE_REQUIRED")
    @Pattern(regexp = "^[0-9]{10}(-?[0-9]{3})?$", message = "INVALID_TAX_CODE")
    private String taxCode;
    @NotNull(message = "LOGO_REQUIRED")
    private MultipartFile logo;
    @NotNull(message = "LICENSE_IMAGE_REQUIRED")
    private MultipartFile licenseImage;
}