package com.jobplatform.job_recruitment_system.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jobplatform.job_recruitment_system.enums.MessageType;
import com.jobplatform.job_recruitment_system.enums.Role;
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
    @JsonProperty("role")
    private Role role;
    @JsonProperty("messageType")
    private MessageType messageType;
    @JsonProperty("fileUrl")
    private String fileUrl;
    @JsonProperty("fileName")
    private String fileName;

}
