package com.jobplatform.job_recruitment_system.repositories;

import com.jobplatform.job_recruitment_system.dtos.JobRecommendationDTO;
import com.jobplatform.job_recruitment_system.models.MatchScore;
import com.jobplatform.job_recruitment_system.models.MatchScoreId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchScoreRepository  extends JpaRepository<MatchScore, MatchScoreId> {
    @Query("SELECT new com.jobplatform.job_recruitment_system.dtos.JobRecommendationDTO(ms.job, ms.score, ms.match_details) " +
            "FROM MatchScore ms WHERE ms.cv.id = :cvId ORDER BY ms.score DESC")
    List<JobRecommendationDTO> findRecommendedJobsByCvId(@Param("cvId") Long cvId);

    @Query(value = """
    SELECT ms.* FROM match_scores ms
    JOIN jobs j ON ms.job_id = j.id
    WHERE ms.cv_id = :cvId
      AND (
          :keyword IS NULL 
          OR TRIM(:keyword) = '' 
          OR j.search_vector @@ plainto_tsquery('simple', :keyword)
      )
    ORDER BY ms.score DESC
    """, nativeQuery = true)
    List<MatchScore> findRecommendedEntitiesByKeyword(@Param("cvId") Long cvId, @Param("keyword") String keyword);

    @Query("Select new com.jobplatform.job_recruitment_system.dtos.JobRecommendationDTO(ms.job, ms.score, ms.match_details) " +
            "FROM MatchScore ms WHERE ms.cv.id = :cvId and ms.job.id =:jobId")
    JobRecommendationDTO findRecommendedJobsByJobId(@Param("jobId") Long jobId,@Param("cvId") Long cvId );
}
