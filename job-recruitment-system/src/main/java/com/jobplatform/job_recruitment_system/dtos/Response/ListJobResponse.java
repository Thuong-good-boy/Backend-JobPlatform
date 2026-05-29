package com.jobplatform.job_recruitment_system.dtos.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListJobResponse {
    private Long id;
    private String title;
    private String location;
    private Integer salaryMin;
    private Integer salaryMax;
    private String status;
    private LocalDateTime createdAt;
    private Set<String> skills;
    private Integer totalApplications;
    private Integer newApplications;
    private LocalDateTime trendingUntil;
    private Integer viewCount;
    private Integer clickCount;

}
