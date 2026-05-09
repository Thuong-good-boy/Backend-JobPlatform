package com.jobplatform.job_recruitment_system.dtos.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class ReportSubmitRequest {

    @NotNull(message = "ID mục tiêu báo cáo không được để trống")
    private Long targetId;

    @NotNull(message = "Lý do báo cáo không được để trống")
    private Long reasonId;

    @Size(max = 1000, message = "Mô tả tối đa 1000 ký tự")
    private String description;

    @Size(max = 3, message = "Chỉ được upload tối đa 3 ảnh")
    private List<MultipartFile> files;
}
