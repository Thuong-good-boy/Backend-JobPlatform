package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.dtos.Response.ChatMessageResponse;
import com.jobplatform.job_recruitment_system.dtos.Response.ChatNotificationReponse;
import com.jobplatform.job_recruitment_system.dtos.request.ChatMessageRequest;
import com.jobplatform.job_recruitment_system.enums.Role;
import com.jobplatform.job_recruitment_system.exceptions.AppException;
import com.jobplatform.job_recruitment_system.exceptions.ErrorCode;
import com.jobplatform.job_recruitment_system.mapper.ChatMessageMapper;
import com.jobplatform.job_recruitment_system.models.ChatMessage;
import com.jobplatform.job_recruitment_system.models.ChatRoom;
import com.jobplatform.job_recruitment_system.models.User;
import com.jobplatform.job_recruitment_system.repositories.ChatMessageRepository;
import com.jobplatform.job_recruitment_system.repositories.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private  final ChatMessageMapper chatMessageMapper;
    private final ChatRoomRepository chatRoomRepository;
    private  final  UserService userService;
    private final SimpMessagingTemplate messagingTemplate;
    private  final  ChatRoomService chatRoomService;
    @Transactional
    public void save(Long jobId,ChatMessageRequest  request){
        ChatRoom room = chatRoomRepository.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_001));
        ChatMessage message = new ChatMessage();
        message.setRoom(room);
        System.out.println("có id ko : " + request.getSenderId());
        message.setSenderId(request.getSenderId());
        message.setRead(false);
        message.setContent(request.getContent());
        ChatMessage chatMessage = chatMessageRepository.save(message);
        room.setLastMessageAt(LocalDateTime.now());
        ChatRoom chatRoom =  chatRoomRepository.save(room);
        ChatMessageResponse chatMessageResponse = new ChatMessageResponse(
                chatMessage.getId(),
                chatMessage.getContent(),
                chatMessage.getSenderId(),
                chatMessage.getSentAt()

        );
        messagingTemplate.convertAndSend(
                "/topic/room/"+chatRoom.getId(),
                chatMessageResponse

        );
        Long receiverId= chatRoomService.getReceiverId(chatRoom.getId(),message.getSenderId());
        ChatNotificationReponse reponse = new ChatNotificationReponse();
        reponse.setRoomId(chatRoom.getId());
        reponse.setSenderName(chatRoom.getCompany().getCompanyName());
        reponse.setType("CHAT_PING");
        if (receiverId != null) {
            messagingTemplate.convertAndSend("/topic/chat-notifications/" + receiverId, reponse);
        }
    }
    public Slice<ChatMessage> findByRoomIdOrderBySentAtAsc(Long roomId, int page , int size){
        Pageable pageable= PageRequest.of(page, size);
        return  chatMessageRepository.findByRoomIdOrderBySentAtDesc(roomId, pageable);
    }
    public  void markMessagesAsRead(Long roomId){
        Long currentUserId = userService.getCurrentUserId();
        chatMessageRepository.markMessagesAsRead(roomId,currentUserId);
    }




}
