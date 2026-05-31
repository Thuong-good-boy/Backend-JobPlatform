package com.jobplatform.job_recruitment_system.dtos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
class BetterPhrasingDto {
    private String original;
    private String suggestion;
}
