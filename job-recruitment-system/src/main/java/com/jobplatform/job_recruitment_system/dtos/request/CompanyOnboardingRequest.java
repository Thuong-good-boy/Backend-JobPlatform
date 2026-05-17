package com.jobplatform.job_recruitment_system.dtos.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyOnboardingRequest {
    @NotNull(message = "LICENSE_IMAGE_REQUIRED")
    private MultipartFile licenseImage;
    private String website;
    @Size(max = 2000, message = "DESCRIPTION_TOO_LONG")
    private String description;
}