package com.jobplatform.job_recruitment_system.repositories;

import com.jobplatform.job_recruitment_system.dtos.Response.RegistrationTrendsResponse;
import com.jobplatform.job_recruitment_system.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long userId);
    @Query(value = """
    with in_3month as (
    	select generate_series(
    		DATE_TRUNC('month', CURRENT_DATE - INTERVAL '2 months'),
            DATE_TRUNC('month', CURRENT_DATE),
    		'1 month'::interval
    	)::date as month_series
    )
      select  CAST('Tháng ' || EXTRACT(MONTH FROM m.month_series) AS VARCHAR) AS month_series,
      COALESCE(j.total_company, 0) AS total_company,
      COALESCE(j.total_candidate, 0) AS total_candidate
      from in_3month m LEFT join
      (select DATE_TRUNC('month',u.created_at):: date as user_month,
      COUNT(CASE WHEN u.role = 'COMPANY' THEN 1 END) AS total_company,
    	COUNT(CASE WHEN u.role = 'CANDIDATE' THEN 1 END) AS total_candidate
      from users u
      where u.created_at >= DATE_TRUNC('month',CURRENT_DATE - INTERVAL '2 months')
      group by  DATE_TRUNC('month',u.created_at)::date
      ) j on m.month_series = j.user_month
      order by  m.month_series
    
""",nativeQuery = true)
    List<RegistrationTrendsResponse> getRegistrationTrendsResponse();
}
