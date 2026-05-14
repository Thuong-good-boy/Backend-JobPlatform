package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.enums.NotificationType;
import com.jobplatform.job_recruitment_system.exceptions.AppException;
import com.jobplatform.job_recruitment_system.exceptions.ErrorCode;
import com.jobplatform.job_recruitment_system.models.Application;
import com.jobplatform.job_recruitment_system.models.Notification;
import com.jobplatform.job_recruitment_system.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private  final  UserService userService;
    private  final SimpMessagingTemplate messagingTemplate;
    private  final NotificationRepository notificationRepository;
    private  final MessageSource messageSource;
    public void sendNotification(Long recipientId, Long senderId, NotificationType type, Map<String,Object> metadata,Object... args){
        Locale locale = LocaleContextHolder.getLocale();
        String titleKey ="noti.title."+type.name();
        String messageKey="noti.message."+type.name();

        String title = messageSource.getMessage(titleKey,null,locale);
        String message= messageSource.getMessage(messageKey, args,locale);

        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setSenderId(senderId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setMetadata(metadata);
        Notification saveNotification1= notificationRepository.save(notification);
        messagingTemplate.convertAndSend(
                "/topic/notifications/"+saveNotification1.getRecipientId(),
                saveNotification1
        );

    }
    public Page<Notification> findByRecipientIdOrderByCreatedAtDesc(int page, int size){
        Long userId = userService.getCurrentUserId();
        if(userId== null){
            throw new AppException(ErrorCode.AUTH_008);
        }
        Pageable pageable= PageRequest.of(page, size);
        return  notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId,pageable);
    }
    public  int  countUnread(){
        Long userId = userService.getCurrentUserId();
        if(userId== null){
            throw new AppException(ErrorCode.AUTH_008);
        }
        return notificationRepository.countUnread(userId);
    }
    public  void statusRead(Long id){
         notificationRepository.markAsRead(id);
    }
    public  void markAllAsRead(){
        Long userId = userService.getCurrentUserId();
        if(userId== null){
            throw new AppException(ErrorCode.AUTH_008);
        }
        notificationRepository.markAllAsRead(userId);
    }

}
