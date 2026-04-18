package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.exceptions.AppException;
import com.jobplatform.job_recruitment_system.exceptions.ErrorCode;
import com.jobplatform.job_recruitment_system.models.ChatMessage;
import com.jobplatform.job_recruitment_system.models.ChatRoom;
import com.jobplatform.job_recruitment_system.repositories.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private  final  ChatRoomService chatRoomService;
    public ChatMessage save(Long roomID, ChatMessage chatMessage){
        ChatRoom room = chatRoomService.findById(roomID)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_001));

        chatMessage.setRoom(room);
        return chatMessageRepository.save(chatMessage);
    }
    public List<ChatMessage> findByRoomIdOrderBySentAtAsc(Long roomId){
        return  chatMessageRepository.findByRoomIdOrderBySentAtAsc(roomId);
    }
}
