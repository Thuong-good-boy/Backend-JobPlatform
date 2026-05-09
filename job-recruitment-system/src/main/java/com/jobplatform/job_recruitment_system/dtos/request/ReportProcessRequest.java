package com.jobplatform.job_recruitment_system.dtos.request;

import com.jobplatform.job_recruitment_system.enums.ReportStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportProcessRequest {

    @NotNull(message = "Trạng thái xử lý không được để trống")
    private ReportStatus status;

    @NotBlank(message = "Admin phải nhập ghi chú xử lý")
    private String adminNote;
}
