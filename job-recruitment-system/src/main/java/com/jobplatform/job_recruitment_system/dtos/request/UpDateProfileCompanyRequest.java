package com.jobplatform.job_recruitment_system.dtos.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpDateProfileCompanyRequest {
    @Size(max = 255, message = "COMPANY_NAME_TOO_LONG")
    private String companyname;
    @URL(message = "INVALID_WEBSITE_URL")
    private String website;
    @Size(max = 255, message = "ADDRESS_TOO_LONG")
    private String address;
    @Size(max = 5000, message = "DESCRIPTION_TOO_LONG")
    private String description;

}
