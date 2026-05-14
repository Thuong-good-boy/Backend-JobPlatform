package com.jobplatform.job_recruitment_system.controllers;

import com.jobplatform.job_recruitment_system.dtos.request.ChatMessageRequest;
import com.jobplatform.job_recruitment_system.models.ChatMessage;
import com.jobplatform.job_recruitment_system.services.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
@Validated
public class ChatWebSocketController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage/{roomId}")
    public void sendMessage(@Payload ChatMessageRequest request,
                            @DestinationVariable Long roomId
    ) {
         chatMessageService.save(roomId, request);
    }
    @MessageMapping("/chat.sendTyPing/{roomId}")
    public  void typing(@Payload Map<String, Object> payload, @DestinationVariable Long roomId){
        payload.put("type","TYPING");
        System.out.println("ty ping cos len ");
        messagingTemplate.convertAndSend(
                "/topic/room/"+roomId,
                (Object) payload
        );

    }
}