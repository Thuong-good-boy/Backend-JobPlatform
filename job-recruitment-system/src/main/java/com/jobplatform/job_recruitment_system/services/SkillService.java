package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.exceptions.AppException;
import com.jobplatform.job_recruitment_system.exceptions.ErrorCode;
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
    public  void create(String nameSkill){
        Skill skill= new Skill(nameSkill);
        skillRepository.save(skill);
    }
    public  void delete(Long id){
        Skill skill = skillRepository.findById(id).orElseThrow(()->new AppException(ErrorCode.SKILL_01));
        skillRepository.delete(skill);
    }
    public void updateSkil(Long id , String nameSkill){
        Skill skill = skillRepository.findById(id).orElseThrow(()->new AppException(ErrorCode.SKILL_01));
        skill.setSkillName(nameSkill);
        skillRepository.save(skill);
    }
}
