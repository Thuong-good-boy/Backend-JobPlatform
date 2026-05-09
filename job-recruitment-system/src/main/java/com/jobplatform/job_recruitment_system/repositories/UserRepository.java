package com.jobplatform.job_recruitment_system.repositories;

import com.jobplatform.job_recruitment_system.dtos.Response.AdminUserResponse;
import com.jobplatform.job_recruitment_system.dtos.Response.RegistrationTrendsResponse;
import com.jobplatform.job_recruitment_system.enums.Role;
import com.jobplatform.job_recruitment_system.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    Optional<User> findByEmailAndActiveTrue(String email);
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

@Query("SELECT new com.jobplatform.job_recruitment_system.dtos.Response.AdminUserResponse(" +
        "u.id, u.fullName, u.role, u.active, u.authProvider, " +
        "com.companyName, com.website, com.description, com.address, com.taxCode, com.remainingBoosts, com.isVerified, " +
        "c.title, c.bio, c.experienceYears, c.location, c.expectedSalaryMin, c.expectedSalaryMax, c.isPublic) " +
        "FROM User u " +
        "LEFT JOIN Company com ON com.user.id = u.id " +
        "LEFT JOIN Candidate c ON c.user.id = u.id " +
        "WHERE u.role <> com.jobplatform.job_recruitment_system.enums.Role.ADMIN " +
        "AND u.role = :role " +
        "AND (LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
Page<AdminUserResponse> getAdminUserResponse(Pageable pageable, @Param("role") Role role, @Param("search") String search);


    @Modifying
    @Transactional
    @Query("update User u set  u.active=false where u.id = :userId")
    void blockUser(@Param("userId") Long userId);
    @Modifying
    @Transactional
    @Query("update User u set  u.active=true where u.id = :userId")
    void unBlockUser(@Param("userId") Long userId);
    @Query("select u.active from User u where u.id=:userId")
    Boolean findActiveById(@Param("userId") Long userId);

}
