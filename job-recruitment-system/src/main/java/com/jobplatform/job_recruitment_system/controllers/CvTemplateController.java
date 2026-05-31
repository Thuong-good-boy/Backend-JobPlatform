package com.jobplatform.job_recruitment_system.controllers;
import com.jobplatform.job_recruitment_system.dtos.Response.TemplateReponse;
import com.jobplatform.job_recruitment_system.models.CvTemplate;
import com.jobplatform.job_recruitment_system.services.CvTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cv-templates")
@RequiredArgsConstructor
public class CvTemplateController {
    final private CvTemplateService cvTemplateService;
    @GetMapping
    public ResponseEntity<TemplateReponse> getActiveTemplates() {
        TemplateReponse templateReponse = cvTemplateService.getActiveTemplates();
        return ResponseEntity.ok(templateReponse);
    }

    @PostMapping
    public ResponseEntity<CvTemplate> createTemplate(@RequestBody CvTemplate template) {
        CvTemplate newTemplate = cvTemplateService.createTemplate(template);
        return ResponseEntity.ok(newTemplate);
    }
    @GetMapping("/all-cv-template")
    public ResponseEntity<Slice<CvTemplate>> getAllTemplates(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "3") int size) {

        Slice<CvTemplate> templateResponse = cvTemplateService.getAllTemplate(page, size);

        return ResponseEntity.ok(templateResponse);
    }
}
