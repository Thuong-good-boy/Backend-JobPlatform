package com.jobplatform.job_recruitment_system.repositories;

import com.jobplatform.job_recruitment_system.models.UnlockedCv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnlockedCvRepository extends JpaRepository<UnlockedCv, Long> {
    boolean existsByCompanyIdAndCandidateId(Long companyId, Long candidateId);
}
