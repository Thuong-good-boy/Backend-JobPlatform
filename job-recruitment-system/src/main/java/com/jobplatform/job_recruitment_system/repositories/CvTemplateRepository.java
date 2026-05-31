package com.jobplatform.job_recruitment_system.repositories;

import com.jobplatform.job_recruitment_system.models.CvTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CvTemplateRepository extends JpaRepository<CvTemplate, Long> {

    List<CvTemplate> findByIsActiveTrueOrderByUsageCountDesc();

    Optional<CvTemplate> findByTemplateKey(String templateKey);
}