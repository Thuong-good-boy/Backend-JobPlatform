package com.jobplatform.job_recruitment_system.repositories;

import com.jobplatform.job_recruitment_system.dtos.Response.JobsLast3MonthsResponse;
import com.jobplatform.job_recruitment_system.models.Job;
import com.jobplatform.job_recruitment_system.models.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {


    List<Job> findAll();

    @Query(value = """
        SELECT * FROM jobs 
        WHERE :keyword IS NULL 
           OR TRIM(:keyword) = '' 
           OR search_vector @@ plainto_tsquery('simple', :keyword)
        ORDER BY created_at DESC
        Limit 10
        """, nativeQuery = true)
    List<Job> searchJobs(@Param("keyword") String keyword);
    // Dùng ?1 nghĩa là lấy cái tham số đầu tiên (Long companyId) ném vào đây
    @Query("SELECT j FROM Job j WHERE j.company.id = ?1 ORDER BY j.createdAt DESC")
    List<Job> findByCompanyIdCustom(Long companyId);

    Optional<Job> findById(@Param("jobId")Long jobId);
    long countByCompanyUserId(Long userId);
    long countByCompanyUserIdAndStatus(Long userId, JobStatus status);
    @Query(value = """
     select count(j.id)
    from jobs j
    where j.company_id = :companyId
    and j.status ='OPEN'
    """,nativeQuery = true)
    long totalActiveJobs(@Param("companyId") Long companyId);

    @Query(value = """
     select count(j.id)
    from jobs j
    where j.status ='OPEN'
    """,nativeQuery = true)
    long totalActiveJobsAdmin();

    @Query(value = """
    with in_3month as (
    	select generate_series(
    		DATE_TRUNC('month', CURRENT_DATE - INTERVAL '2 months'),
            DATE_TRUNC('month', CURRENT_DATE),
    		'1 month'::interval
    	)::date as month_series
    )
      select  CAST('Tháng ' || EXTRACT(MONTH FROM m.month_series) AS VARCHAR) AS month_series, 
              COALESCE(j.total_jobs, 0) AS total_jobs
      from in_3month m LEFT join
      (select  DATE_TRUNC('month',j.created_at):: date as job_month ,count(j.id) as total_jobs
      from jobs j
      where j.created_at>= DATE_TRUNC('month',CURRENT_DATE - interval '2 months')
      GROUP by DATE_TRUNC('month',j.created_at):: date
      ) j on m.month_series = j.job_month
      order by  m.month_series
""", nativeQuery = true)
    List<JobsLast3MonthsResponse> getJobsLast3MonthsReponse();

}
