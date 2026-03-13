package com.jobplatform.job_recruitment_system.repositories;

import com.jobplatform.job_recruitment_system.models.SavedJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {

    Optional<SavedJob> findByUserIdAndJobId(Long userId, Long jobId);

    boolean existsByJobIdAndUserId(Long userId,Long jobId);

    void deleteByJobIdAndUserId(Long jobId, Long userId);

    List<SavedJob> findByUserId(Long userId);
}