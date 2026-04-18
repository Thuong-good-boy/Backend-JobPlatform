package com.jobplatform.job_recruitment_system.dtos;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopCompanyResponseDTO {

    private Long id;

    @JsonProperty("company_name")
    private String companyName;

    @JsonProperty("logo_url")
    private String logoUrl;

    private String description;

    @JsonProperty("job_count")
    private Long jobCount;
}