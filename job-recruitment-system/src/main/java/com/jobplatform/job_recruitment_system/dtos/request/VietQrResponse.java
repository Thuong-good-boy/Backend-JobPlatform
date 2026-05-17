package com.jobplatform.job_recruitment_system.dtos.request;

import com.jobplatform.job_recruitment_system.models.VietQrDataRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VietQrResponse {
    private String code;
    private String desc;
    private VietQrDataRequest data;
}
