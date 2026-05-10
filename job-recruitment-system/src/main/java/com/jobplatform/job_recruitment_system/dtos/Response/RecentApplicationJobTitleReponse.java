package com.jobplatform.job_recruitment_system.dtos.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecentApplicationJobTitleReponse {
        private Long id;
        private String fullname;
        private String jobTitle;
        private String status;
        private LocalDateTime appliedAt;

}
