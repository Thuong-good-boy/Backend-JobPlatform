package com.jobplatform.job_recruitment_system.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class GoogleLoginRequest {
    @NotBlank(message = "TOKEN_REQUIRED")
    private String credential;
    @NotBlank(message = "ROLE_REQUIRED")
    @Pattern(
            regexp = "^(CANDIDATE|COMPANY|ADMIN)$",
            message="INVALID_ROLE"
    )
    private  String role;
}
