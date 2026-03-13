package com.jobplatform.job_recruitment_system.repositories;

import com.jobplatform.job_recruitment_system.models.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    boolean existsByJobIdAndCvId(Long jobId, Long cvId);
    boolean existsByJobIdAndCv_User_Id(Long jobId, Long userId);
    List<Application> findByCv_User_IdOrderByAppliedAtDesc(Long userId);

    List<Application> findByJobId(Long jobId);
}