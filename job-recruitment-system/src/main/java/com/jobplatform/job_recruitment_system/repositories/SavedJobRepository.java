package com.jobplatform.job_recruitment_system.repositories;

import com.jobplatform.job_recruitment_system.models.SavedJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {

    Optional<SavedJob> findByUserIdAndJobId(Long userId, Long jobId);

    boolean existsByJobIdAndUserId(Long jobId, Long userId);

    void deleteByJobIdAndUserId(Long jobId, Long userId);

    // SỬA DÒNG NÀY: Để việc mới lưu lên đầu
    List<SavedJob> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Modifying
    @Query("DELETE FROM SavedJob s WHERE s.job.id = :jobId")
    void deleteByJobId(@Param("jobId") Long jobId);
    @Query("""
    select count(s.id)
    from Job j join  SavedJob s on j.id = s.job.id
    where j.company.userId = :companyId
""")
    Long getTotalJobSave(@Param("companyId") Long companyId);

}