package com.jobplatform.job_recruitment_system.dtos.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.A;
import org.hibernate.validator.constraints.URL;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CandidateProfileRequest {
    @NotBlank(message = "FULLNAME_REQUIRED")
    private String fullName;
    @NotBlank(message = "EMAIL_REQUIRED")
    @Pattern(
            regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$",
            message = "INVALID_EMAIL"
    )
    private String email;
    @URL(message = "AVATAR_MUST_BE_VALID_URL")
    private String avatarUrl;
    @Size(max = 255, message = "TITLE_TOO_LONG")
    private String title;
    @Size(max = 2000,message = "BIO_TOO_LONG")
    private String bio;
    @Min(value = 0, message = "EXPERIENCE_YEARS_MIN_0")
    @Max(value = 50, message = "EXPERIENCE_YEARS_MAX_50")
    private Integer experienceYears;
    @Size(max = 255, message = "LOCALTION_TOOLONG")
    private String location;
    @Min(value =  0 , message = "SALARY_MUST_BE_POSITIVE")
    private Integer expectedSalaryMin;
    @Min(value = 0, message = "SALARY_MUST_BE_POSITIVE")
    private Integer expectedSalaryMax;
    @NotNull(message = "IS_PUBLIC_REQUIRED")
    private Boolean isPublic;
}
