package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.models.Skill;
import com.jobplatform.job_recruitment_system.repositories.SkillRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class SkillService {
    private  final SkillRepository skillRepository;
    public List<Skill> getall(){
        return  skillRepository.findAll();
    }
}
