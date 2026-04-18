package com.jobplatform.job_recruitment_system.mapper;

import com.jobplatform.job_recruitment_system.dtos.request.JobPostRequest;
import com.jobplatform.job_recruitment_system.models.Job;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "Spring")
public interface JobMapper {
    Job fromJobPostRequest(JobPostRequest jobPostRequest);
    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "url_logo", ignore = true)
    void updateJob(JobPostRequest source, @MappingTarget Job target);}
