package com.jobplatform.job_recruitment_system.dtos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EducationDto {
    @JsonProperty("degree")
    private String degree;

    @JsonProperty("university")
    private String university;

    @JsonProperty("duration")
    private String duration;

    @JsonProperty("details")
    private String details;
}