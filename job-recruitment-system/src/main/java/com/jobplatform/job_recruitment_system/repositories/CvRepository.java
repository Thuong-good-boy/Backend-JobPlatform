package com.jobplatform.job_recruitment_system.repositories;

import com.jobplatform.job_recruitment_system.models.Cv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CvRepository extends JpaRepository<Cv, Long> {
    List<Cv> findAllByUser_IdAndActiveTrueOrderByCreatedAtDesc(Long userId);
    Optional<Cv> findFirstByUser_IdAndActiveTrueOrderByCreatedAtDesc(Long userId);
}
