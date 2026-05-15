package com.jobplatform.job_recruitment_system.repositories;

import com.jobplatform.job_recruitment_system.dtos.Response.JobRecommendationResponse;
import com.jobplatform.job_recruitment_system.models.MatchScore;
import com.jobplatform.job_recruitment_system.models.MatchScoreId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchScoreRepository  extends JpaRepository<MatchScore, MatchScoreId> {
    @Query("SELECT new com.jobplatform.job_recruitment_system.dtos.Response.JobRecommendationResponse(ms.job, ms.score, ms.match_details, ms.job.company.companyName) " +
            "FROM MatchScore ms WHERE ms.cv.id = :cvId AND ms.job.status = com.jobplatform.job_recruitment_system.enums.JobStatus.OPEN ORDER BY ms.score DESC")
    Page<JobRecommendationResponse> findRecommendedJobsByCvId(@Param("cvId") Long cvId, Pageable pageable);

    @Query("SELECT new com.jobplatform.job_recruitment_system.dtos.Response.JobRecommendationResponse(ms.job, ms.score, ms.match_details, ms.job.company.companyName) " +
            "FROM MatchScore ms " +
            "WHERE ms.cv.id = :cvId " +
            "AND ms.job.status = com.jobplatform.job_recruitment_system.enums.JobStatus.OPEN " +
            "AND ms.job.company.userId = :companyId " +
            "ORDER BY ms.score DESC")
    List<JobRecommendationResponse> findRecommendedJobsByCvIdAndCompanyId(@Param("cvId") Long cvId, @Param("companyId") Long companyId);


    @Query(value = """
    SELECT ms.* FROM match_scores ms
    JOIN jobs j ON ms.job_id = j.id
    WHERE ms.cv_id = :cvId
        AND j.status = 'OPEN'
      AND (
          :keyword IS NULL 
          OR TRIM(:keyword) = '' 
          OR j.search_vector @@ plainto_tsquery('simple', :keyword)
      )
    ORDER BY ms.score DESC
    """, nativeQuery = true)
    List<MatchScore> findRecommendedEntitiesByKeyword(@Param("cvId") Long cvId, @Param("keyword") String keyword);

    @Query("Select new com.jobplatform.job_recruitment_system.dtos.Response.JobRecommendationResponse(ms.job, ms.score, ms.match_details, ms.job.company.companyName) " +
            "FROM MatchScore ms WHERE ms.cv.id = :cvId and ms.job.id =:jobId")
    JobRecommendationResponse findRecommendedJobsByJobId(@Param("jobId") Long jobId, @Param("cvId") Long cvId );
}
