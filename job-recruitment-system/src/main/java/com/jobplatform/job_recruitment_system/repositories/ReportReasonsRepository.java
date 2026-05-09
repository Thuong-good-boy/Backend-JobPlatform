package com.jobplatform.job_recruitment_system.repositories;

import com.jobplatform.job_recruitment_system.models.ReportReasons;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportReasonsRepository extends JpaRepository<ReportReasons,Long> {

    @Query("select r from ReportReasons r  where r.targetType = com.jobplatform.job_recruitment_system.enums.ReportTargetType.COMPANY")
    List<ReportReasons> findForCompany();
    @Query("select r from ReportReasons r  where r.targetType = com.jobplatform.job_recruitment_system.enums.ReportTargetType.CANDIDATE")
    List<ReportReasons> findForCandidate();
    @Query("select r from ReportReasons r  where r.targetType = com.jobplatform.job_recruitment_system.enums.ReportTargetType.JOB")
    List<ReportReasons> findForJob();
}
