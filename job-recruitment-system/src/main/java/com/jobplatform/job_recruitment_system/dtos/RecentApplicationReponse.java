package com.jobplatform.job_recruitment_system.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecentApplicationReponse {
    private Long id;
    private String fullname;
    private String jobTitle;
    private String status;        // Trạng thái (APPLIED, REVIEWING, etc.)
    private LocalDateTime appliedAt; // Thời gian nộp
}