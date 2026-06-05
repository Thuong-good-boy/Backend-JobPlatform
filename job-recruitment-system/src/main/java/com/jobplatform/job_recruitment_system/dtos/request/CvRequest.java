package com.jobplatform.job_recruitment_system.dtos.request;

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

    @JsonProperty("fullName")
    private String fullName;

    @JsonProperty("jobTitle")
    private String jobTitle;

    @JsonProperty("avatarUrl")
    private String avatarUrl;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("email")
    private String email;

    @JsonProperty("address")
    private String address;

    @JsonProperty("github")
    private String github;

    @JsonProperty("linkedin")
    private String linkedin;

    @JsonProperty("skills")
    private List<String> skills;

    @JsonProperty("experiences")
    private List<ExperienceDto> experiences;

    @JsonProperty("projects")
    private List<ProjectDto> projects;

    @JsonProperty("educations")
    private List<EducationDto> educations;

    @JsonProperty("languages")
    private List<LanguageDto> languages;

    @JsonProperty("templateName")
    private String templateName;

    @JsonProperty("homepage")
    private String homepage;

    @JsonProperty("twitter")
    private String twitter;

    @JsonProperty("gitlab")
    private String gitlab;

    @JsonProperty("orcid")
    private String orcid;

    @JsonProperty("philosophy")
    private String philosophy;

    @JsonProperty("achievements")
    private List<AchievementDto> achievements;

    @JsonProperty("dayOfLife")
    private List<WheelChartDto> dayOfLife;

    @JsonProperty("publications")
    private List<PublicationDto> publications;

    public String getTemplateName() {
        return templateName != null ? templateName : "AltaCV";
    }
}