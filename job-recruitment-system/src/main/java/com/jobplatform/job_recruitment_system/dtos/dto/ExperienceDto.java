package com.jobplatform.job_recruitment_system.dtos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExperienceDto {
    @JsonProperty("role")
    private String role;

    @JsonProperty("company")
    private String company;

    @JsonProperty("duration")
    private String duration;

    @JsonProperty("location")
    private String location;

    @JsonProperty("description")
    private List<String> description;
}