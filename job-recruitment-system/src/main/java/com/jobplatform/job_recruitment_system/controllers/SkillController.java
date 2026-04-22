package com.jobplatform.job_recruitment_system.controllers;

import com.jobplatform.job_recruitment_system.models.Skill;
import com.jobplatform.job_recruitment_system.services.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/Skill")
public class SkillController {
    private  final SkillService skillService;
    @GetMapping
    public List<Skill> getSkills(){
        return skillService.getall();
    }

}
