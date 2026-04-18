package com.jobplatform.job_recruitment_system.controllers;

import com.jobplatform.job_recruitment_system.models.ChatMessage;
import com.jobplatform.job_recruitment_system.models.ChatRoom;
import com.jobplatform.job_recruitment_system.repositories.ChatMessageRepository;
import com.jobplatform.job_recruitment_system.repositories.ChatRoomRepository;
import com.jobplatform.job_recruitment_system.services.ChatMessageService;
import com.jobplatform.job_recruitment_system.services.ChatRoomService;
import com.jobplatform.job_recruitment_system.services.UserService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@Validated
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private  final UserService userService;

    // 1. Lấy danh sách phòng chat của 1 ứng viên
    @GetMapping("/rooms/candidate")
    public ResponseEntity<List<ChatRoom>> getRoomsByCandidate() {
        List<ChatRoom> rooms = chatRoomService.findByCandidateId();
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/{roomId}/messages")
    public ResponseEntity<List<ChatMessage>> getChatHistory(

            @PathVariable Long roomId) {
        List<ChatMessage> messages = chatMessageService.findByRoomIdOrderBySentAtAsc(roomId);
        return ResponseEntity.ok(messages);
    }
    @GetMapping("/rooms/company")
    public ResponseEntity<List<ChatRoom>> getRoomsByCompany() {
        List<ChatRoom> rooms = chatRoomService.findByCompanyId();
        return ResponseEntity.ok(rooms);
    }
}