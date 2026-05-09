package com.jobplatform.job_recruitment_system.dtos.Response;

import com.jobplatform.job_recruitment_system.enums.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ReportResponse {
        private Long id;
        private String reporterName;
        private String reporterEmail;

        private String reasonTitle;

        private Long targetId;

        private String description;
        private List<String> evidenceImages;

        private ReportStatus status;
        private String adminNote;

        private LocalDateTime createdAt;

}
