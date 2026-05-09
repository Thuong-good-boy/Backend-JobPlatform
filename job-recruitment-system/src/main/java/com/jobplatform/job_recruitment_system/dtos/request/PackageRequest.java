package com.jobplatform.job_recruitment_system.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jobplatform.job_recruitment_system.enums.PackageStatus;
import com.jobplatform.job_recruitment_system.enums.PackageType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
@NoArgsConstructor
@Data
public class PackageRequest {
    @NotBlank(message = "Tên gói dịch vụ không được để trống")
    private String name;

    @NotNull(message = "Loại gói không được để trống")
    private PackageType type;

    @NotNull(message = "Giá tiền không được để trống")
    @Min(value = 0, message = "Giá tiền không được nhỏ hơn 0 VNĐ")
    private BigDecimal price;

    @NotNull(message = "Thời hạn không được để trống")
    @Min(value = 1, message = "Thời hạn gói tối thiểu phải là 1 ngày")
    @JsonProperty("duration_days")
    private Integer durationDays;

    @NotNull(message = "Giới hạn lượt nộp CV không được để trống")
    @Min(value = 0, message = "Giới hạn lượt nộp không được là số âm")
    @JsonProperty("apply_limit")
    private Integer applyLimit;

    @NotNull(message = "Giới hạn lượt đăng tin không được để trống")
    @Min(value = 0, message = "Giới hạn đăng/đẩy tin không được là số âm")
    @JsonProperty("job_post_limit")
    private Integer jobPostLimit;

    @NotNull(message = "Trạng thái không được để trống")
    private PackageStatus status;
}
