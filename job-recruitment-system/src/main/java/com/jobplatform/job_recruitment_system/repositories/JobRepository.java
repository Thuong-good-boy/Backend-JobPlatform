package com.jobplatform.job_recruitment_system.repositories;

import com.jobplatform.job_recruitment_system.dtos.Response.CompanyJobsByCandidateResponse;
import com.jobplatform.job_recruitment_system.dtos.Response.JobsLast3MonthsResponse;
import com.jobplatform.job_recruitment_system.models.Job;
import com.jobplatform.job_recruitment_system.enums.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> , RevisionRepository<Job, Long, Integer> {

    @Query("select j from Job j where j.company.id = :companyId")
    Page<Job> findByJCompanyId(Pageable pageable, @Param("companyId") Long companyId);
    @Modifying
    @Transactional
    @Query("UPDATE Job j SET j.viewCount = COALESCE(j.viewCount, 0) + 1 WHERE j.id IN :jobIds")
    void incrementViewCountForJobs(@Param("jobIds") List<Long> jobIds);
    @Query(value = """
     SELECT * FROM jobs
         WHERE (pinning_until < CURRENT_TIMESTAMP OR is_pinning = false OR is_pinning IS NULL)
           AND status = 'OPEN'
         ORDER BY last_boosted_at DESC
    """, nativeQuery = true)
    Slice<Job> findAllJobsOpen(Pageable pageable);
    @Query("""
        select j from Job j where j.title like %:search% and j.status = :status order by j.createdAt desc
    """)
    Page<Job> findAllByStatusForAdmin(Pageable pageable, @Param("search") String search, @Param("status") JobStatus status);
    @Query("""
        select j from Job j order by j.createdAt desc
    """)
    Page<Job> findAllForAdmin(Pageable pageable);
    @Query(value = """
        SELECT * FROM jobs 
        WHERE (:keyword IS NULL 
           OR TRIM(:keyword) = '' 
           OR search_vector @@ plainto_tsquery('simple', :keyword))
        ORDER BY 
           (CASE 
               WHEN   pinning_until > CURRENT_TIMESTAMP THEN 1 
               ELSE 0 
           END) DESC, 
           
           (CASE 
               WHEN :keyword IS NULL OR TRIM(:keyword) = '' THEN 0 
               ELSE ts_rank(search_vector, plainto_tsquery('simple', :keyword)) 
           END) DESC,
           created_at DESC
        """, countQuery = """
        SELECT count(*) FROM jobs
        WHERE (:keyword IS NULL
        OR TRIM(:keyword) = '' 
        OR search_vector @@ plainto_tsquery('simple', :keyword))
        """, nativeQuery = true)
    Slice<Job> searchJobs(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT j FROM Job j WHERE j.company.id = ?1 ORDER BY j.createdAt DESC")
    List<Job> findByCompanyIdCustom(Long companyId);

    Optional<Job> findById(@Param("jobId") Long jobId);

    long countByCompany_User_Id(Long userId);

    long countByCompany_User_IdAndStatus(Long userId, JobStatus status);

    @Query(value = """
     select count(j.id)
    from jobs j
    where j.company_id = :companyId
    and j.status ='OPEN'
    """, nativeQuery = true)
    long totalActiveJobs(@Param("companyId") Long companyId);

    @Query(value = """
     select count(j.id)
    from jobs j
    where j.status ='OPEN'
    """, nativeQuery = true)
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

    @Query(value = """
    SELECT 
        j.id, 
        j.title, 
        j.location, 
        CAST(j.salary_min AS INTEGER) AS salaryMin, 
        CAST(j.salary_max AS INTEGER) AS salaryMax, 
        j.status, 
        j.created_at AS createdAt,
        (SELECT ARRAY_AGG(s.skill_name) FROM job_skills js JOIN skills s ON js.skill_id = s.id WHERE js.job_id = j.id) AS skills,
        (SELECT CAST(COUNT(*) AS INTEGER) FROM applications a WHERE a.job_id = j.id) AS totalApplications,
        (SELECT CAST(COUNT(*) AS INTEGER) FROM applications a WHERE a.job_id = j.id AND a.status = 'NEW') AS newApplications,
        ms.score AS matchScore,
        ms.match_details AS matchDetails
    FROM jobs j 
    LEFT JOIN match_scores ms ON j.id = ms.job_id AND ms.cv_id = CAST(:cvId AS BIGINT) 
    WHERE j.company_id = :companyId
    """, nativeQuery = true)
    List<CompanyJobsByCandidateResponse> findCompanyJobs(@Param("companyId") Long companyId, @Param("cvId") Long cvId);

    @Query(value = """
    Select * from jobs j 
    where j.search_vector @@ websearch_to_tsquery('simple', :skillsQuery)
      ORDER BY ts_rank(j.search_vector, websearch_to_tsquery('simple', :skillsQuery)) DESC
      LIMIT 10
    """, nativeQuery = true)
    List<Job> findTop10MatchingJob(@Param("skillsQuery") String skillsQuery);

    @Query(value = """
    SELECT COUNT(j.id) < 2
    FROM jobs j
    WHERE j.company_id = :companyId;
    """, nativeQuery = true)
    boolean followNotPro(@Param("companyId") Long companyId);

    @Transactional
    @Modifying
    @Query("UPDATE Job j SET j.isTrending = false WHERE j.isTrending = true AND j.trendingUntil < CURRENT_TIMESTAMP")
    int resetExpiredTrendingJobs();

    @Query(value = """
    (
        SELECT *
        FROM jobs
        WHERE pinning_until > CURRENT_TIMESTAMP
          AND status = 'OPEN'
          AND search_vector @@ to_tsquery('simple', :skillQuery)
        ORDER BY
          ts_rank(search_vector, to_tsquery('simple', :skillQuery)) DESC,
          last_boosted_at DESC
        LIMIT 3
    )
    UNION ALL
    (
        SELECT *
        FROM jobs
        WHERE pinning_until > CURRENT_TIMESTAMP
          AND status = 'OPEN'
          AND NOT EXISTS (
              SELECT 1
              FROM jobs j
              WHERE j.pinning_until > CURRENT_TIMESTAMP
                AND j.status = 'OPEN'
                AND j.search_vector @@ to_tsquery('simple', :skillQuery)
          )
        ORDER BY last_boosted_at ASC
        LIMIT 3
    )
""", nativeQuery = true)
    List<Job> findTop3ProJobsBySkills(
            @Param("skillQuery") String skillQuery
    );

    @Query(value = """
    SELECT * FROM jobs 
    WHERE pinning_until > CURRENT_TIMESTAMP 
      AND status = 'OPEN'
    ORDER BY last_boosted_at ASC
    LIMIT 3
""", nativeQuery = true)
    List<Job> findTop3ProJobsRoundRobin();
}