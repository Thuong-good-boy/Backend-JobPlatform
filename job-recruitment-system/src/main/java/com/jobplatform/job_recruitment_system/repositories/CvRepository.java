package com.jobplatform.job_recruitment_system.repositories;

import com.jobplatform.job_recruitment_system.dtos.Response.CvsLast3MonthsResponse;
import com.jobplatform.job_recruitment_system.models.Cv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CvRepository extends JpaRepository<Cv, Long> {
    List<Cv> findAllByUser_IdAndActiveTrueOrderByCreatedAtDesc(Long userId);
    Optional<Cv> findFirstByUser_IdAndActiveTrueOrderByCreatedAtDesc(Long userId);

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

}
