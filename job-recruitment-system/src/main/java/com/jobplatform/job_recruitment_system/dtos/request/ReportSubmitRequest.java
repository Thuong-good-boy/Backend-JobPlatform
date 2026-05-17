package com.jobplatform.job_recruitment_system.dtos.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jobplatform.job_recruitment_system.enums.TargetType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportSubmitRequest {

    @NotNull(message = "ID mục tiêu báo cáo không được để trống")
    private Long targetId;
    @JsonProperty("targettype")
    private TargetType targettype;
    private Long reasonId;
    @Size(max = 200, message = "Mô tả tối đa 1000 ký tự")
    private String customReason;
    @Size(max = 1000, message = "Mô tả tối đa 1000 ký tự")
    private String description;
    @Size(max = 3, message = "Chỉ được upload tối đa 3 ảnh")
    private List<MultipartFile> files;
}
