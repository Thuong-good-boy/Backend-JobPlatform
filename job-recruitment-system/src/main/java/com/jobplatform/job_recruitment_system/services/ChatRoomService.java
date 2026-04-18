package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.models.ChatMessage;
import com.jobplatform.job_recruitment_system.models.ChatRoom;
import com.jobplatform.job_recruitment_system.repositories.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private  final ChatRoomRepository chatRoomRepository;
    private  final  UserService userService;
    public Optional<ChatRoom> findById(Long roomid){
        return  chatRoomRepository.findById(roomid);
    }
    public List<ChatRoom> findByCandidateId(){
        Long userID= userService.getCurrentUserId();
        return  chatRoomRepository.findByCandidateId(userID);

    }
    public  List<ChatRoom> findByCompanyId(){
        Long companyId = userService.getCurrentUserId();
        return  chatRoomRepository.findByCompanyId(companyId);
    }

}
