package com.jobplatform.job_recruitment_system.dtos.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageResponse {
    private Long id;
    private String content;
    private Long senderId;
    private LocalDateTime sentAt;
}
