package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.exceptions.ErrorCode;
import com.jobplatform.job_recruitment_system.models.Application;
import com.jobplatform.job_recruitment_system.models.Notification;
import com.jobplatform.job_recruitment_system.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private  final  UserService userService;
    private  final NotificationRepository notificationRepository;
    public List<Notification> findByUserIdOrderByCreatedAtDesc(){
        Long userId = userService.getCurrentUserId();
        if(userId== null){
            throw new RuntimeException("User chưa đăng nhập");
        }
        return  notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
