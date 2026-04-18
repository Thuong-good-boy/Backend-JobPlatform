package com.jobplatform.job_recruitment_system.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "FULLNAME_REQUIRED")
    private String fullName;

    @NotBlank(message = "EMAIL_REQUIRED")
    @Pattern(
            regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$",
            message = "INVALID_EMAIL"
    )
    private String email;

    @NotBlank(message = "PASSWORD_REQUIRED")
    @Pattern(
            regexp = "^[A-Za-z0-9]{6,}$",
            message = "INVALID_PASSWORD"
    )
    private String password;

    @NotBlank(message = "ROLE_REQUIRED")
    @Pattern(
            regexp = "^(CANDIDATE|COMPANY)$",
            message = "INVALID_ROLE"
    )
    private String role;
}