package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.dtos.Response.SkillValidationResponse;
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
    private  final AiOcrService aiOcrService;
    public List<Skill> getall(){

        return  skillRepository.findAll();
    }
    public  void create(String nameSkill){
        String cleanInput = nameSkill.trim();
        boolean check = skillRepository.existsBySkillNameIgnoreCase(cleanInput);
        if(check){
            throw  new AppException(ErrorCode.SKILL_02);
        }

         if (cleanInput != null) {
                Skill newSkill = new Skill();
                newSkill.setSkillName(cleanInput);
                skillRepository.save(newSkill);
            }

    }
    public  SkillValidationResponse createByUsers(String nameSkill){
        String cleanInput = nameSkill.trim();
        boolean check = skillRepository.existsBySkillNameIgnoreCase(cleanInput);
        if(check){
            throw  new AppException(ErrorCode.SKILL_02);
        }
        SkillValidationResponse skillValidationResponse = aiOcrService.validateAndNormalizeNewSkill(cleanInput);

        if (skillValidationResponse.isValid()) {
            if (!skillRepository.existsBySkillNameIgnoreCase(skillValidationResponse.getStandardizedName())) {
                Skill newSkill = new Skill();
                newSkill.setSkillName(skillValidationResponse.getStandardizedName());
                skillRepository.save(newSkill);
            }
        } else {
            throw new AppException(ErrorCode.SKILL_03);
        }
        return skillValidationResponse;
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
