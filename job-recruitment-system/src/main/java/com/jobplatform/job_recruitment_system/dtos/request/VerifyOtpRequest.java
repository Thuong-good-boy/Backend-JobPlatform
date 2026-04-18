package com.jobplatform.job_recruitment_system.dtos.request;

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
public class VerifyOtpRequest {

    @NotBlank(message = "EMAIL_REQUIRED")
    @Pattern(
            regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$",
            message = "INVALID_EMAIL"
    )
    private String email;

    @NotBlank(message = "OTP_REQUIRED")
    @Pattern(
            regexp = "^[0-9]{6}$",
            message = "INVALID_OTP"
    )
    private String otp;
}