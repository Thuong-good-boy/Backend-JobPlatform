package com.jobplatform.job_recruitment_system.dtos.request;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CandidateUpdateRequest {

    @NotBlank(message = "Họ và tên không được để trống")
    @JsonProperty("full_name")
    private String fullName;

    @NotBlank(message = "Chức danh không được để trống")
    @JsonProperty("title")
    private String title;

    @JsonProperty("location")
    private String location;

    @NotNull(message = "Số năm kinh nghiệm không được để trống")
    @Min(value = 0, message = "Số năm kinh nghiệm không được là số âm")
    @JsonProperty("experience_years")
    private Integer experienceYears;

    @Min(value = 0, message = "Mức lương tối thiểu không được âm")
    @JsonProperty("expected_salary_min")
    private Integer expectedSalaryMin;

    @Min(value = 0, message = "Mức lương tối đa không được âm")
    @JsonProperty("expected_salary_max")
    private Integer expectedSalaryMax;

    @JsonProperty("bio")
    private String bio;

    @NotNull(message = "Trạng thái công khai không được để null")
    @JsonProperty("is_public")
    private Boolean isPublic;
}