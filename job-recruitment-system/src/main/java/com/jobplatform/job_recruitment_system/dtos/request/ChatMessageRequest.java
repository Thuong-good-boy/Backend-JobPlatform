package com.jobplatform.job_recruitment_system.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageRequest {
    @JsonProperty("senderId")
    private Long senderId;
    @JsonProperty("content")
    private String content;

}
