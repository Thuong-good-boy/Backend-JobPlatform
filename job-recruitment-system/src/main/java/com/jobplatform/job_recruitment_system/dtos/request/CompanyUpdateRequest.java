package com.jobplatform.job_recruitment_system.dtos.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CompanyUpdateRequest {

    @NotBlank(message = "Họ và tên người đại diện không được để trống")
    @JsonProperty("full_name")
    private String fullName;

    @NotBlank(message = "Tên doanh nghiệp không được để trống")
    @JsonProperty("company_name")
    private String companyName;

    @NotBlank(message = "Mã số thuế không được để trống")
    @JsonProperty("tax_code")
    private String taxCode;

    @JsonProperty("website")
    private String website;

    @NotNull(message = "Lượt đẩy tin không được để trống")
    @Min(value = 0, message = "Lượt đẩy tin không được là số âm")
    @JsonProperty("remaining_boosts")
    private Integer remainingBoosts;

    @JsonProperty("address")
    private String address;

    @JsonProperty("description")
    private String description;

    @NotNull(message = "Trạng thái xác thực không được để null")
    @JsonProperty("is_verified")
    private Boolean isVerified;
}