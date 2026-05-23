package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.dtos.Response.ChatMessageResponse;
import com.jobplatform.job_recruitment_system.dtos.Response.ChatNotificationReponse;
import com.jobplatform.job_recruitment_system.dtos.request.ChatMessageRequest;
import com.jobplatform.job_recruitment_system.enums.MessageType;
import com.jobplatform.job_recruitment_system.enums.Role;
import com.jobplatform.job_recruitment_system.exceptions.AppException;
import com.jobplatform.job_recruitment_system.exceptions.ErrorCode;
import com.jobplatform.job_recruitment_system.mapper.ChatMessageMapper;
import com.jobplatform.job_recruitment_system.models.ChatMessage;
import com.jobplatform.job_recruitment_system.models.ChatRoom;
import com.jobplatform.job_recruitment_system.models.User;
import com.jobplatform.job_recruitment_system.repositories.CandidateRepository;
import com.jobplatform.job_recruitment_system.repositories.ChatMessageRepository;
import com.jobplatform.job_recruitment_system.repositories.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private  final  UserService userService;
    private final SimpMessagingTemplate messagingTemplate;
    private  final  ChatRoomService chatRoomService;
    private  final FileUploadService fileUploadService;
    @Transactional
    public void save(Long jobId,ChatMessageRequest  request){
        ChatRoom room = chatRoomRepository.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_001));
        ChatMessage message = new ChatMessage();
        message.setRoom(room);
        message.setSenderId(request.getSenderId());
        message.setRead(false);
        message.setContent(request.getContent());
        message.setMessageType(request.getMessageType());
        message.setFileName(request.getFileName());
        message.setFileUrl(request.getFileUrl()!=null? request.getFileUrl() : MessageType.TEXT.toString());

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

        if(request.getRole().equals(Role.COMPANY)){

            reponse.setSenderName(chatRoom.getCompany().getCompanyName());

        }else{
            User user = userService.getUserId(request.getSenderId()).orElseThrow(()-> new AppException(ErrorCode.AUTH_008));
            reponse.setSenderName(user.getFullName());
        }
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

    public  String getUpFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_01);
        }
        String urlFile = null;
        try {
            urlFile = fileUploadService.uploadFile(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return urlFile;
    }
    public  void deleteFile(String file){
        if (file == null || file.isEmpty()) {
            throw  new AppException(ErrorCode.FILE_02);
        }
        fileUploadService.deleteImage(file);
    }


}
