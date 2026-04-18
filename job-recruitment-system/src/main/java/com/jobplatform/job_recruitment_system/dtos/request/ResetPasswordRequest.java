package com.jobplatform.job_recruitment_system.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class ResetPasswordRequest {

    @NotBlank(message = "RESETTOKEN_REQUIRED")
    private  String resetToken;
    @NotBlank(message = "PASSWORD_REQUIRED")
    @Pattern(
            regexp = "^[A-Za-z0-9]{6,}$",
            message = "INVALID_PASSWORD"
    )
    private String newPassword;
    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}