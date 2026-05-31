package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.dtos.Response.TemplateReponse;
import com.jobplatform.job_recruitment_system.exceptions.AppException;
import com.jobplatform.job_recruitment_system.exceptions.ErrorCode;
import com.jobplatform.job_recruitment_system.models.*;
import com.jobplatform.job_recruitment_system.repositories.CandidateRepository;
import com.jobplatform.job_recruitment_system.repositories.CvRepository;
import com.jobplatform.job_recruitment_system.repositories.CvTemplateRepository;
import com.jobplatform.job_recruitment_system.repositories.SkillRepository;
import lombok.AllArgsConstructor;
import org.mapstruct.ap.shaded.freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class CvTemplateService {


    final private CvTemplateRepository cvTemplateRepository;
    final  private SkillRepository skillRepository;
    final  private CvRepository repository;
    final  private  UserService userService;
    final  private CandidateRepository candidateRepository;
    public TemplateReponse getActiveTemplates() {
        TemplateReponse templateReponse = new TemplateReponse();
        templateReponse.setCvTemplateList(cvTemplateRepository.findByIsActiveTrueOrderByUsageCountDesc());
        templateReponse.setSkillList(skillRepository.findAll());
        Long userId= userService.getCurrentUserId();

        Cv  cv= repository.findFirstByUser_IdAndActiveTrueOrderByCreatedAtDesc(userId).orElse(null);
        if(cv !=null) {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> cvData = objectMapper.readValue(cv.getCvData(), Map.class);
            templateReponse.setData(cvData);
        }
    return templateReponse;
    }

    public void incrementUsage(String templateKey) {
        cvTemplateRepository.findByTemplateKey(templateKey).ifPresent(template -> {
            template.setUsageCount(template.getUsageCount() + 1);
            cvTemplateRepository.save(template);
        });
    }
    public Slice<CvTemplate> getAllTemplate(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return cvTemplateRepository.findAll(pageable);
    }
    public CvTemplate createTemplate(CvTemplate template) {
        return cvTemplateRepository.save(template);
    }
}