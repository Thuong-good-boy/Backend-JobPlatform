package com.jobplatform.job_recruitment_system.dtos.Response;

import lombok.Builder;
import lombok.Data;

@Data
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private  boolean isNew;

}