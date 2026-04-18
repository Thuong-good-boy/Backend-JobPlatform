package com.jobplatform.job_recruitment_system.repositories;

import com.jobplatform.job_recruitment_system.models.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    Optional<Candidate> findByUserId(Long userId);

    // Tìm kiếm những ứng viên công khai hồ sơ (dùng cho Headhunting)
    List<Candidate> findByIsPublicTrue();

}
