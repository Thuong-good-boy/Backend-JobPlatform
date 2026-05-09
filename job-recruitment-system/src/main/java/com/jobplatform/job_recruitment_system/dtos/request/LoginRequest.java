package com.jobplatform.job_recruitment_system.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class LoginRequest {

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
            regexp = "^(CANDIDATE|COMPANY|ADMIN)$",
            message = "INVALID_ROLE"
    )
    private String role;
    @JsonProperty("captchaToken")
    private  String captchaToken;
    private  boolean remember;
}
