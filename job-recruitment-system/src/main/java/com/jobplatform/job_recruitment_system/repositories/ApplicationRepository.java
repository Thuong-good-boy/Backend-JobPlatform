package com.jobplatform.job_recruitment_system.repositories;

import com.jobplatform.job_recruitment_system.dtos.Response.ApplicationOnlyJobResponse;
import com.jobplatform.job_recruitment_system.dtos.Response.ChartDataResponse;
import com.jobplatform.job_recruitment_system.dtos.Response.RecentApplicationReponse;
import com.jobplatform.job_recruitment_system.enums.AppStatus;
import com.jobplatform.job_recruitment_system.models.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    boolean existsByJobIdAndCv_User_Id(Long jobId, Long userId);
    List<Application> findByCv_User_IdOrderByAppliedAtDesc(Long userId);
    List<Application> findTop7ByJob_Company_User_IdOrderByAppliedAtDesc(Long userId);
    long countByJob_Company_User_Id(Long userId);
    @Modifying
    @Query("DELETE FROM Application a WHERE a.job.id = :jobId")
    void deleteByJobId(@Param("jobId") Long jobId);

    @Query("""
        select new com.jobplatform.job_recruitment_system.dtos.Response.ApplicationOnlyJobResponse(
                a.id, a.fullName , a.status,
                a.appliedAt, c.id, c.fileUrl, m.score, m.match_details,
                        a.job.company.companyName, a.user.email
                )
        from Application a left join a.cv c
             left join MatchScore m on(c.id = m.cv.id AND m.job.id = a.job.id)
        where a.job.id=:jobId  
        ORDER BY a.appliedAt DESC
        """)
    List<ApplicationOnlyJobResponse> findListApplicationIncludeMatch(@Param("jobId") Long jobId);
    @Query("""
        select count(a.id)
        from Application a
        where a.job.company.id = :companyId
    """)
    Long totalApplications(@Param("companyId") Long companyId);

    @Query(value = """
    with in_7_day as (
    	select generate_series(
    		CURRENT_DATE - INTERVAL '6 days',
    		CURRENT_DATE,
    		'1 day':: interval
    	)::date as date_series
    )
    	select To_Char(d.date_series,'DD/MM') as date,
    	count (a.id) as apply,
    	count(*)  FILTER(where a.status ='INTERVIEW' ) as interview
    	from in_7_day d LEFT join
    	(select app.id, app.status,  app.applied_at:: date as appliedAt
    	from applications app join jobs j on app.job_id = j.id
    	where j.company_id = :companyId)  a on  d.date_series = a.appliedAt
    	GROUP by d.date_series
    	order by d.date_series asc
""", nativeQuery= true)
    List<ChartDataResponse> totalApplicationsin7day(@Param("companyId") Long companyId);
    @Query(value = """
    SELECT 
        a.id AS id, 
        u.full_name AS name, 
        j.title AS job, 
        a.applied_at AS appliedAt, 
        a.status AS status
    FROM applications a
    JOIN jobs j ON a.job_id = j.id
    JOIN users u ON a.user_id = u.id 
    WHERE j.company_id = :companyId
    ORDER BY a.applied_at DESC
    LIMIT 5
""", nativeQuery = true)
    List<RecentApplicationReponse> getTop5RecentApplications(@Param("companyId") Long companyId);
    @Query(value = """
    select count(a.id)
    from applications a
    where a.job_id = :jobId
    and a.status ='APPLIED'
""",nativeQuery = true)
    Long getnewApplications(@Param("jobId") Long jobId);
    @Query(value = """
    select count(a.id)
    from applications a
    where a.job_id = :jobId
""",nativeQuery = true)
    Long gettotalApplications(@Param("jobId") Long jobId);
    @Query(value = """
    select count(a.id)
    from applications a
""",nativeQuery = true)
    Long gettotalApplicationsAdmin();
    @Query(value = """
    select count(a.id) from applications a where a.user_id = :userId
""",nativeQuery = true)
    Integer getCountApply(@Param("userId") Long userId);
    @Query("select count(a.id) from Application a where a.cv.id=:cvId")
    Long countByCvId(@Param("cvId") Long cvId);
}

