package com.jobplatform.job_recruitment_system.repositories;

import com.jobplatform.job_recruitment_system.dtos.Response.ApplicationOnlyJobResponse;
import com.jobplatform.job_recruitment_system.models.AppStatus;
import com.jobplatform.job_recruitment_system.models.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    boolean existsByJobIdAndCvId(Long jobId, Long cvId);
    boolean existsByJobIdAndCv_User_Id(Long jobId, Long userId);
    List<Application> findByCv_User_IdOrderByAppliedAtDesc(Long userId);
    List<Application> findTop7ByJob_Company_UserIdOrderByAppliedAtDesc(Long userId);
    List<Application> findByJobId(Long jobId);
    long countByJob_Company_UserId(Long userId);
    List<Application> findByJobIdAndStatus(Long jobId, AppStatus status);
    @Modifying
    @Query("DELETE FROM Application a WHERE a.job.id = :jobId")
    void deleteByJobId(@Param("jobId") Long jobId);

    @Query("""
        select new com.jobplatform.job_recruitment_system.dtos.Response.ApplicationOnlyJobResponse(
                a.id, a.fullname, a.phone,  a.address , a.status, a.coverLetter,
                a.appliedAt, c.id, c.fileUrl, m.score, m.match_details
                )
        from Application a left join a.cv c
             left join MatchScore m on(c.id = m.cv.id AND m.job.id = a.job.id)
        where a.job.id=:jobId  
        ORDER BY a.appliedAt DESC
        """)
    List<ApplicationOnlyJobResponse> findListApplicationIncludeMatch(@Param("jobId") Long jobId);
}