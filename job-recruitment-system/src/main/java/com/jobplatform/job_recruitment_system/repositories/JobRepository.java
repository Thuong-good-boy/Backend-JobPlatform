package com.jobplatform.job_recruitment_system.repositories;

import com.jobplatform.job_recruitment_system.models.Company;
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
}
