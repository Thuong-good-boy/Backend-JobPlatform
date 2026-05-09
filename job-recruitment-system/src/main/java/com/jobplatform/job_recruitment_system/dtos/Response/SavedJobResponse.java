package com.jobplatform.job_recruitment_system.dtos.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavedJobResponse {

    private Long id;            // ID của bản ghi SavedJob (để lát làm nút Bỏ lưu)
    private Long jobId;         // ID của công việc

    private String jobTitle;    // Tên công việc
    private String companyName; // Tên công ty
    private String logoUrl;     // Logo công ty

    private String location;    // Địa điểm
    private String salary;      // Mức lương
    private String savedDate;   // Ngày lưu (Format dd/MM/yyyy)
}