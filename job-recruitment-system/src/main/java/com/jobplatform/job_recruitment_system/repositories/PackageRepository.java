package com.jobplatform.job_recruitment_system.repositories;

import com.jobplatform.job_recruitment_system.dtos.Response.CandidateProResponse;
import com.jobplatform.job_recruitment_system.dtos.Response.PackageCompany;
import com.jobplatform.job_recruitment_system.models.Package;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackageRepository extends JpaRepository<Package,Long> {
     Optional<Package> findById(Long id);
    @Query(value = """
 select p.id, p.duration_days as durationDays,p.price 
 from packages p 
 where p."type" = 'CANDIDATE_PRO'
""",nativeQuery = true)
    List<CandidateProResponse> getCadidateProResponse();
    @Query(value = """
 select p.id,p."name", p.duration_days as durationDays ,p.job_post_limit as jobPostLimit, p.price
from packages p
where p."type" = 'COMPANY_PRO'
or p."type" = 'JOB_BOOST'
""",nativeQuery = true)
    List<PackageCompany> getpackageCompany();
}
