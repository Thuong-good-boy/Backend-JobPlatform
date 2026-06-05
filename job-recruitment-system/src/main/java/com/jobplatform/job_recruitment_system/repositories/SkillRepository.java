package com.jobplatform.job_recruitment_system.repositories;

import com.jobplatform.job_recruitment_system.models.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    Optional<Skill> findBySkillName(String skillName);
    @Query("select s.skillName from Skill s ")
    List<String> getALlSkillName();
    boolean existsBySkillNameIgnoreCase(String skillName);
}
