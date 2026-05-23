package com.jobplatform.job_recruitment_system.repositories;

import com.jobplatform.job_recruitment_system.models.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("select c from ChatRoom c where c.job.id = :jobId AND c.company.id = :companyId AND c.candidate.id = :candidateId")
    Optional<ChatRoom> findByJobIdAndCompanyIdAndCandidateId(@Param("jobId") Long jobId, @Param("companyId") Long companyId, @Param("candidateId") Long candidateId);

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.company.user.id = :userId OR cr.candidate.user.id = :userId")
    Page<ChatRoom> findUserChatRooms(@Param("userId") Long userId, Pageable pageable);

    @Query("select c from ChatRoom c where c.candidate.user.id = :userId order by c.lastMessageAt desc")
    Page<ChatRoom> findByCandidateUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("select c from ChatRoom c where c.company.id = :companyId order by  c.lastMessageAt desc")
    Page<ChatRoom> findByCompanyId(@Param("companyId") Long companyId, Pageable pageable);
    Boolean existsByCompanyIdAndCandidateIdAndJobId(Long companyId, Long candidateId, Long jobId);

}