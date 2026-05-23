package com.jobplatform.job_recruitment_system.repositories;

import com.jobplatform.job_recruitment_system.dtos.Response.CvsLast3MonthsResponse;
import com.jobplatform.job_recruitment_system.models.Cv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CvRepository extends JpaRepository<Cv, Long> {
    List<Cv> findAllByUser_IdAndActiveTrueOrderByCreatedAtDesc(Long userId);
    Optional<Cv> findFirstByUser_IdAndActiveTrueOrderByCreatedAtDesc(Long userId);
    @Query(value = """
        SELECT c.*
        FROM cvs c
        JOIN (
            SELECT DISTINCT us.user_id
            FROM user_subscriptions us
            JOIN packages p
                ON us.package_id = p.id
            WHERE p.type IN ('COMPANY_PRO', 'CANDIDATE_PRO')
              AND us.status = 'ACTIVE'
              AND us.end_date >= CURRENT_TIMESTAMP
        ) pro_users
            ON c.user_id = pro_users.user_id
        JOIN (
            SELECT user_id, MAX(id) AS latest_cv_id
            FROM cvs
            GROUP BY user_id
        ) latest_cv
            ON c.id = latest_cv.latest_cv_id
        """, nativeQuery = true)
    List<Cv> findCvPro();
    @Query(value = """
    with in_3month as (
    	select generate_series(
    		DATE_TRUNC('month', CURRENT_DATE - INTERVAL '2 months'),
            DATE_TRUNC('month', CURRENT_DATE),
    		'1 month'::interval
    	)::date as month_series
    )
      select  CAST('Tháng ' || EXTRACT(MONTH FROM m.month_series) AS VARCHAR) AS month_series,
              COALESCE(j.total_cvs, 0) AS total_cvs
      from in_3month m LEFT join
      (select DATE_TRUNC('month',a.applied_at):: date as cv_month, count(a.id) as total_cvs
      from applications a
      where a.applied_at >= DATE_TRUNC('month',CURRENT_DATE - INTERVAL '2 months')
      group by  DATE_TRUNC('month',a.applied_at)::date
      ) j on m.month_series = j.cv_month
      order by  m.month_series
""", nativeQuery = true)
    List<CvsLast3MonthsResponse> getCvsLast3MonthsResponse();

    @Query(value = "select * from cvs where user_id = :userId limit 1", nativeQuery = true)
    Optional<Cv> getCvByUserId(@Param("userId") Long userId);



}
