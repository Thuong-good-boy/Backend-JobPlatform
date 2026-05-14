package com.jobplatform.job_recruitment_system.repositories;

import com.jobplatform.job_recruitment_system.models.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Slice<ChatMessage> findByRoomIdOrderBySentAtDesc(Long roomId, Pageable pageable);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.room.id = :roomId AND m.senderId != :currentUserId AND m.isRead = false")
    long countUnreadMessages(@Param("roomId") Long roomId, @Param("currentUserId") Long currentUserId);

    @Modifying
    @Transactional
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.room.id = :roomId AND m.senderId != :currentUserId AND m.isRead = false")
    void markMessagesAsRead(@Param("roomId") Long roomId, @Param("currentUserId") Long currentUserId);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.room.candidate.id = :userId AND m.senderId != :userId AND m.isRead = false")
    long countTotalUnreadForCandidate(@Param("userId") Long userId);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.room.company.userId = :userId AND m.senderId != :userId AND m.isRead = false")
    long countTotalUnreadForCompany(@Param("userId") Long userId);

}