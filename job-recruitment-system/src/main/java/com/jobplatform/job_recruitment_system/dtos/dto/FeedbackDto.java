package com.jobplatform.job_recruitment_system.dtos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackDto {
    private List<BetterPhrasingDto> better_phrasing;
}