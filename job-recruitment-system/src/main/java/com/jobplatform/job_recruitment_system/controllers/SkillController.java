package com.jobplatform.job_recruitment_system.controllers;

import com.jobplatform.job_recruitment_system.models.Skill;
import com.jobplatform.job_recruitment_system.services.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/Skill")
public class SkillController {
    private  final SkillService skillService;
    @GetMapping
    public List<Skill> getSkills(){
        return skillService.getall();
    }
    @PostMapping
    public void createSkill(@RequestBody Map<String,String> request){
        String nameSkill = request.get("skill_name");
        skillService.create(nameSkill);
    }
    @DeleteMapping("/{id}")
    public void deleteSkill(@PathVariable Long id){
        skillService.delete(id);
    }
    @PutMapping("/{id}")
    public  void updateSkill(@PathVariable Long id,@RequestBody Map<String,String> request){
        String nameSkill=  request.get("skill_name");
        skillService.updateSkil(id,nameSkill);
    }
}
