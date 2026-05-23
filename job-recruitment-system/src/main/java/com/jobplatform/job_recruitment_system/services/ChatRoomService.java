package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.enums.Role;
import com.jobplatform.job_recruitment_system.exceptions.AppException;
import com.jobplatform.job_recruitment_system.exceptions.ErrorCode;
import com.jobplatform.job_recruitment_system.models.Candidate;
import com.jobplatform.job_recruitment_system.models.ChatMessage;
import com.jobplatform.job_recruitment_system.models.ChatRoom;
import com.jobplatform.job_recruitment_system.models.User;
import com.jobplatform.job_recruitment_system.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final UserService userService;
    private final JobRepository jobRepository;
    private final CompanyService companyService;
    private final CandidateRepository candidateRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final CompanyRepository companyRepository; // Thêm repo này để lấy ID thực

    public Optional<ChatRoom> findById(Long roomid) {
        return chatRoomRepository.findById(roomid);
    }

    public Slice<ChatRoom> findByCandidateId(int page, int size) {
        Long userId = userService.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by("lastMessageAt").descending());
        Slice<ChatRoom> chatRooms = chatRoomRepository.findByCandidateUserId(userId, pageable);

        chatRooms.getContent().forEach(chatRoom -> {
            Long unread = chatMessageRepository.countUnreadMessages(chatRoom.getId(), userId);
            chatRoom.setUnreadCount(unread);
        });
        return chatRooms;
    }

    public Page<ChatRoom> findByCompanyId(int page, int size) {
        Long userId = userService.getCurrentUserId();
        Long realCompanyId = companyRepository.findByUser_Id(userId)
                .map(com -> com.getId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_012)); // Hoặc error code tương ứng

        Pageable pageable = PageRequest.of(page, size);
        return chatRoomRepository.findByCompanyId(realCompanyId, pageable);
    }

    public void createRoomIfNotExist(Long jobId, Long companyId, Long candidateId) {
        ChatRoom chatRoom = chatRoomRepository.findByJobIdAndCompanyIdAndCandidateId(jobId, companyId, candidateId).orElse(null);
        if (chatRoom == null) {
            ChatRoom newRoom = new ChatRoom();
            newRoom.setJob(jobRepository.getReferenceById(jobId));
            newRoom.setCompany(companyService.getCompanyById(companyId));
            newRoom.setCandidate(candidateRepository.getReferenceById(candidateId));
            chatRoomRepository.save(newRoom);
        }
    }

    public long getTotalUnreadCount() {
        Long userId = userService.getCurrentUserId();
        User user = userService.getReferenceById(userId);
        if (Role.CANDIDATE.equals(user.getRole())) {
            return chatMessageRepository.countTotalUnreadForCandidate(userId);
        } else {
            return chatMessageRepository.countTotalUnreadForCompany(userId);
        }
    }

    public Long getReceiverId(Long roomId, Long senderId) {
        ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow(() -> new AppException(ErrorCode.ROOM_001));

        Long candidateUserId = room.getCandidate().getUser().getId();
        Long companyUserId = room.getCompany().getUser().getId();

        if (senderId.equals(candidateUserId)) {
            return companyUserId;
        } else {
            return candidateUserId;
        }
    }
    public Boolean checkHasChatRoom(Long companyId, Long candidateId, Long jobId){
        return chatRoomRepository.existsByCompanyIdAndCandidateIdAndJobId(companyId, candidateId, jobId);
    }
}