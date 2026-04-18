package com.jobplatform.job_recruitment_system.controllers;

import com.jobplatform.job_recruitment_system.models.ChatMessage;
import com.jobplatform.job_recruitment_system.models.ChatRoom;
import com.jobplatform.job_recruitment_system.repositories.ChatMessageRepository;
import com.jobplatform.job_recruitment_system.repositories.ChatRoomRepository;
import com.jobplatform.job_recruitment_system.services.ChatMessageService;
import com.jobplatform.job_recruitment_system.services.ChatRoomService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.validation.annotation.Validated;

@Controller
@RequiredArgsConstructor
@Validated
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;

    @MessageMapping("/chat.sendMessage/{roomId}")
    @Transactional
    public void sendMessage(
            @DestinationVariable
                    @NotBlank( message = "ROOM_REQUIRED")
            Long roomId, @Payload @NotBlank(message = "CONTENT_REQUIRED") ChatMessage messagePayload) {

        ChatMessage savedMessage = chatMessageService.save(roomId,messagePayload);

        // 3. Bắn tin nhắn lại cho kênh của phòng đó
        // Đường dẫn mà Frontend sẽ subscribe là: /topic/room/{roomId}
        messagingTemplate.convertAndSend("/topic/room/" + roomId, savedMessage);
    }
}