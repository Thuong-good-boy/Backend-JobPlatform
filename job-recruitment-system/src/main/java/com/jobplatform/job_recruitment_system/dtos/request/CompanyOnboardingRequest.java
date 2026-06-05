package com.jobplatform.job_recruitment_system.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("licenseImage")
    private MultipartFile licenseImage;
    @JsonProperty("description")
    private String description;
}