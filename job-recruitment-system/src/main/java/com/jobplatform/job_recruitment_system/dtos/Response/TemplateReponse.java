package com.jobplatform.job_recruitment_system.dtos.Response;

import com.jobplatform.job_recruitment_system.models.CvTemplate;
import com.jobplatform.job_recruitment_system.models.Skill;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateReponse {
    private List<Skill> skillList;
    private  List<CvTemplate> cvTemplateList;
    private Map<String, Object> data;
}
