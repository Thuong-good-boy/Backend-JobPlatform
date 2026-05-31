package com.jobplatform.job_recruitment_system.dtos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExperienceDto {
    private String role;
    private String duration;
    private String company;
    private String location;
    private List<String> description;

}