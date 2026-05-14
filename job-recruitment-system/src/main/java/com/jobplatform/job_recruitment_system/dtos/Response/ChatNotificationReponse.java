package com.jobplatform.job_recruitment_system.dtos.Response;

import com.jobplatform.job_recruitment_system.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatNotificationReponse {
    private Long roomId;
    private String senderName;
    private String type;
}
