package com.jobplatform.job_recruitment_system.controllers;

import com.jobplatform.job_recruitment_system.models.Notification;
import com.jobplatform.job_recruitment_system.repositories.NotificationRepository;
import com.jobplatform.job_recruitment_system.services.NotificationService;
import com.jobplatform.job_recruitment_system.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    @GetMapping
    public ResponseEntity<Page<Notification>> getUserNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Page<Notification> notifications =  notificationService.findByRecipientIdOrderByCreatedAtDesc(page, size);
        return ResponseEntity.ok(notifications);
    }
    @GetMapping("/unread-count")
    public  ResponseEntity<?> getUnreadCount(){
        int count = notificationService.countUnread();
        return ResponseEntity.ok(count);
    }
    @PutMapping("/{id}/read")
    public  ResponseEntity<?> markAsRead(@PathVariable Long id){
        notificationService.statusRead(id);
        return ResponseEntity.ok(Map.of("message","Thông báo đã được đọc"));
    }
    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok(Map.of("message", "Đã đánh dấu đọc tất cả"));
    }

}