package com.jobplatform.job_recruitment_system.dtos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EducationDto {
    private String degree;
    private String university;
    private String duration;
    private String details;
}
