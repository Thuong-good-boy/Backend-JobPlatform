package com.jobplatform.job_recruitment_system.controllers;

import com.jobplatform.job_recruitment_system.models.ChatMessage;
import com.jobplatform.job_recruitment_system.models.ChatRoom;
import com.jobplatform.job_recruitment_system.repositories.ChatMessageRepository;
import com.jobplatform.job_recruitment_system.repositories.ChatRoomRepository;
import com.jobplatform.job_recruitment_system.services.ChatMessageService;
import com.jobplatform.job_recruitment_system.services.ChatRoomService;
import com.jobplatform.job_recruitment_system.services.FileUploadService;
import com.jobplatform.job_recruitment_system.services.UserService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.query.Param;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@Validated
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;



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
    @PostMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public  ResponseEntity<?> uploadfileDate(@RequestParam("file")MultipartFile file){
        String urlFile = chatMessageService.getUpFile(file);
        String tenFileGoc = file.getOriginalFilename();
        return  ResponseEntity.ok(Map.of("fileUrl", urlFile,
                "fileName", tenFileGoc != null ? tenFileGoc : "Attachment"));
    }
    @DeleteMapping("/deleteFile")
    public ResponseEntity<?> deleteFile(@RequestParam("urlFile") String urlFile){
        chatMessageService.deleteFile(urlFile);
        return  ResponseEntity.ok(Map.of("message", "đã xóa thành công."));
    }
}