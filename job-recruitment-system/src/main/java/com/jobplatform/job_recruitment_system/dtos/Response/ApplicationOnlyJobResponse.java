package com.jobplatform.job_recruitment_system.dtos.Response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jobplatform.job_recruitment_system.enums.AppStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationOnlyJobResponse {
    private Long id;
    private String fullname;
    private AppStatus status;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime appliedAt;
    private Long cvId;
    private String cvUrl;
    private Double aiMatchScore;
    private String aiMatchReason;
}
