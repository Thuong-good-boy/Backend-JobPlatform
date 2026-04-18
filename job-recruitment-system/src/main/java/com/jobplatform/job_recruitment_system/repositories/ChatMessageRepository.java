package com.jobplatform.job_recruitment_system.repositories;

import com.jobplatform.job_recruitment_system.models.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // Lấy tin nhắn của 1 phòng, cũ xếp trước, mới xếp sau
    List<ChatMessage> findByRoomIdOrderBySentAtAsc(Long roomId);
}