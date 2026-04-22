package com.jobplatform.job_recruitment_system.mapper;

import com.jobplatform.job_recruitment_system.dtos.Response.ListJobResponse;
import com.jobplatform.job_recruitment_system.dtos.request.JobPostRequest;
import com.jobplatform.job_recruitment_system.models.Job;
import com.jobplatform.job_recruitment_system.models.Skill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "Spring")
public interface JobMapper {
    Job fromJobPostRequest(JobPostRequest jobPostRequest);

    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "url_logo", ignore = true)
    void updateJob(JobPostRequest source, @MappingTarget Job target);

    @Mapping(target = "totalApplications",ignore = true)
    @Mapping(target = "newApplications",ignore = true)
    ListJobResponse fromJobtoListJobResponse(Job job);

    default Set<String> maptoStrings(Set<Skill> skills){
        if(skills == null){return  null;}
        return skills.stream().map(Skill::getSkillName).collect(Collectors.toSet());
    }

}