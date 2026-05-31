package com.jobplatform.job_recruitment_system.dtos.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProjectDto {
    private String name;
    private String role;
    private String duration;
    private List<String> description;
}
