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
import org.springframework.data.domain.Slice;
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

    @GetMapping("/rooms/candidate")
    public ResponseEntity<Slice<ChatRoom>> getRoomsByCandidate(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "10") int size) {
        Slice<ChatRoom> rooms = chatRoomService.findByCandidateId(page, size);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/{roomId}/messages")
    public ResponseEntity<Slice<ChatMessage>> getChatHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @PathVariable Long roomId) {
        Slice<ChatMessage> messages = chatMessageService.findByRoomIdOrderBySentAtAsc(roomId,page,size);
        return ResponseEntity.ok(messages);
    }
    @PutMapping("/rooms/{roomId}/read")
    public void markRoomAsRead(@PathVariable("roomId") Long roomId){
        chatMessageService.markMessagesAsRead(roomId);
    }
    @GetMapping("/rooms/company")
    public ResponseEntity<Slice<ChatRoom>> getRoomsByCompany(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Slice<ChatRoom> rooms = chatRoomService.findByCompanyId(page, size);
        return ResponseEntity.ok(rooms);
    }
    @GetMapping("/rooms/unread-count")
    public ResponseEntity<Long> getTotalUnreadCount() {
        long total = chatRoomService.getTotalUnreadCount();
        return ResponseEntity.ok(total);
    }
}