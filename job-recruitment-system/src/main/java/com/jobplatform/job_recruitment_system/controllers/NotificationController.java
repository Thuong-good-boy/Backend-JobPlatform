package com.jobplatform.job_recruitment_system.controllers;

import com.jobplatform.job_recruitment_system.models.Notification;
import com.jobplatform.job_recruitment_system.repositories.NotificationRepository;
import com.jobplatform.job_recruitment_system.services.NotificationService;
import com.jobplatform.job_recruitment_system.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private  final UserService userService;
    @GetMapping
    public ResponseEntity<List<Notification>> getUserNotifications() {

        List<Notification> notifications =  notificationService.findByUserIdOrderByCreatedAtDesc();
        return ResponseEntity.ok(notifications);
    }
}