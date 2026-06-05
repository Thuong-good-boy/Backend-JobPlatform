package com.jobplatform.job_recruitment_system.dtos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WheelChartDto {
    @JsonProperty("hours")
    private double hours;

    @JsonProperty("textWidth")
    private String textWidth;

    @JsonProperty("color")
    private String color;

    @JsonProperty("text")
    private String text;
}