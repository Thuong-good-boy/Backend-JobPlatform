package com.jobplatform.job_recruitment_system.dtos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AchievementDto {
    @JsonProperty("icon")
    private String icon;
    @JsonProperty("title")
    private String title;
    @JsonProperty("details")
    private String details;
}
