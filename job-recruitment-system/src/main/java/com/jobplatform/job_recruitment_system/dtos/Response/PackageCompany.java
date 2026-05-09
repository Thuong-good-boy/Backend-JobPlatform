package com.jobplatform.job_recruitment_system.dtos.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
public class PackageCompany {
    private  Long id;
    private  String name;
    private  Integer durationDays;
    private  Integer jobPostLimit;
    private BigDecimal price;
}
