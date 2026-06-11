package com.jobplatform.job_recruitment_system.dtos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackDto {

    @JsonProperty("better_phrasing")
    private List<BetterPhrasingDto> betterPhrasing;
    @JsonProperty("spelling_and_grammar")
    private List<Map<String, String>> spellingAndGrammar;
    @JsonProperty("general_advice")
    private List<String> generalAdvice;

}