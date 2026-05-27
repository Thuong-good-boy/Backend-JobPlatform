package com.jobplatform.job_recruitment_system.repositories;

import com.jobplatform.job_recruitment_system.models.Candidate;
import com.jobplatform.job_recruitment_system.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    Optional<Candidate> findByUserId(Long userId);
    @Query(value = """
    select count(a.id)
    from candidates a
""", nativeQuery = true)
    Long gettotalCandidate();
    @Query("select c.user from Candidate c where c.id=:id ")
    User getUserByCandidate(@Param("id") Long id);
    @Query("select c.id from Candidate c where c.user.id=:userId")
    Long getCandidateIdByUSerId(@Param("userId") Long userId);
    @Query(value = """
    SELECT c.* FROM candidates c
    JOIN (
        SELECT *, ROW_NUMBER() OVER(PARTITION BY user_id ORDER BY created_at DESC) as rn
        FROM cvs
    ) cv ON c.user_id = cv.user_id AND cv.rn = 1
    WHERE c.is_public = true
    AND (CAST(:location AS text) IS NULL OR c.location ILIKE CONCAT('%', CAST(:location AS text), '%'))
    AND (CAST(:minExp AS integer) IS NULL OR c.experience_years >= CAST(:minExp AS integer))
    AND (CAST(:keyword AS text) IS NULL OR 
         c.title ILIKE CONCAT('%', CAST(:keyword AS text), '%') OR 
         cv.cv_data->>'summary' ILIKE CONCAT('%', CAST(:keyword AS text), '%')
    )
    AND (
        CAST(:skillsJson AS text) IS NULL 
        OR cv.cv_data @> CAST(:skillsJson AS jsonb)
    )  
    """,
            countQuery = """
    SELECT count(c.id) FROM candidates c
    JOIN (
        -- ĐÃ SỬA DÒNG BÊN DƯỚI: Thêm cv_data vào SELECT
        SELECT user_id, cv_data, ROW_NUMBER() OVER(PARTITION BY user_id ORDER BY created_at DESC) as rn
        FROM cvs
    ) cv ON c.user_id = cv.user_id AND cv.rn = 1
    WHERE c.is_public = true
    AND (CAST(:location AS text) IS NULL OR c.location ILIKE CONCAT('%', CAST(:location AS text), '%'))
    AND (CAST(:minExp AS integer) IS NULL OR c.experience_years >= CAST(:minExp AS integer))
    AND (CAST(:keyword AS text) IS NULL OR 
         c.title ILIKE CONCAT('%', CAST(:keyword AS text), '%') OR 
         cv.cv_data->>'summary' ILIKE CONCAT('%', CAST(:keyword AS text), '%')
    )
    AND (
        CAST(:skillsJson AS text) IS NULL 
        OR cv.cv_data @> CAST(:skillsJson AS jsonb)
    )
    """,
            nativeQuery = true)
    Page<Candidate> searchCandidates(
            @Param("location") String location,
            @Param("minExp") Integer minExperience,
            @Param("keyword") String keyword,
            @Param("skillsJson") String skillsJson,
            Pageable pageable
    );
}
