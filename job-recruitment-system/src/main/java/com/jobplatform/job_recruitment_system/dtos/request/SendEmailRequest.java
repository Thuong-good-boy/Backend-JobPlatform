package com.jobplatform.job_recruitment_system.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SendEmailRequest {
    private String email;
    private String subject;
    private String body;
    private String senderType;
    @JsonProperty("jobId")
    private Long jobId;

}