package com.jobplatform.job_recruitment_system.dtos.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jobplatform.job_recruitment_system.dtos.dto.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CvRequest {

    private String fullName;
    private String jobTitle;
    private String avatarUrl;
    private String phone;
    private String email;
    private String address;
    private String github;
    private String linkedin;
    private String summary;
    private List<String> skills;
    private List<ExperienceDto> experiences;
    private List<ProjectDto> projects;
    private List<EducationDto> educations;
    private List<LanguageDto> languages;
    private List<String> achievements;
    private List<RefereeDto> referees;
    @JsonProperty("templateName")
    private String templateName;

    public String getTemplateName() {
        return templateName != null ? templateName : "AltaCV";
    }
}