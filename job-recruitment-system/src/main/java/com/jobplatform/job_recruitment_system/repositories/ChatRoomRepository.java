package com.jobplatform.job_recruitment_system.repositories;

import com.jobplatform.job_recruitment_system.models.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    @Query("select c from ChatRoom c where c.job.id = :jobId And c.company.userId=:companyId and  c.candidate.id = :candidateId")
   Optional<ChatRoom> findByJobIdAndCompanyIdAndCandidateId(@Param("jobId") Long jobId,@Param("companyId")  Long companyId,@Param("candidateId")  Long candidateId);
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.company.id = :userId OR cr.candidate.id = :userId")
    Page<ChatRoom> findUserChatRooms(@Param("userId") Long userId, Pageable pageable);
    @Query("select c from ChatRoom c where c.candidate.id = :userId")
    Page<ChatRoom> findByCandidateId(@Param("userId") Long userId, Pageable pageable);

    @Query("select c from ChatRoom c where c.company.userId = :companyId")
    Page<ChatRoom> findByCompanyId(@Param("companyId") Long companyId, Pageable pageable);


}