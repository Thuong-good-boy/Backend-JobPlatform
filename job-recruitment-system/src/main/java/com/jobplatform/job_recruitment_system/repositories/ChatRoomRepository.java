package com.jobplatform.job_recruitment_system.repositories;

import com.jobplatform.job_recruitment_system.models.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // SỬA Ở ĐÂY: r.candidate chính là đối tượng User, nên chỉ cần lấy r.candidate.id
    @Query("SELECT r FROM ChatRoom r WHERE r.candidate.id = :userId")
    List<ChatRoom> findByCandidateId(@Param("userId") Long userId);

    // Tìm phòng chat của Company (Giữ nguyên vì đã đúng)
    @Query("SELECT r FROM ChatRoom r WHERE r.company.userId = :companyId")
    List<ChatRoom> findByCompanyId(@Param("companyId") Long companyId);

    // Giữ nguyên vì r.candidate.id đã chuẩn xác với Cách 1
    @Query("SELECT COUNT(r) > 0 FROM ChatRoom r WHERE r.company.userId = :companyId AND r.candidate.id = :candidateId AND r.job.id = :jobId")
    boolean existsByCompanyIdAndCandidateIdAndJobId(
            @Param("companyId") Long companyId,
            @Param("candidateId") Long candidateId,
            @Param("jobId") Long jobId
    );
}