package com.jobplatform.job_recruitment_system.mapper;

import com.jobplatform.job_recruitment_system.dtos.Response.ApplicationOnlyJobResponse;
import com.jobplatform.job_recruitment_system.dtos.Response.AppliedJobResponse;
import com.jobplatform.job_recruitment_system.dtos.Response.RecentApplicationReponse;
import com.jobplatform.job_recruitment_system.dtos.request.ApplicationRequest;
import com.jobplatform.job_recruitment_system.models.Application;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "Spring")
public interface ApplicationMapper {
    @Mapping(target = "appliedAt", source = "appliedAt" , dateFormat = "dd/MM/yyyy HH:mm", defaultValue = "Chưa cập nhật")
    @Mapping(target = "cvId", source = "cv.id")
    @Mapping(target = "cvUrl", source = "cv.fileUrl")
    @Mapping(target = "aiMatchScore" , ignore = true)
    @Mapping(target = "aiMatchReason", ignore = true)
    ApplicationOnlyJobResponse toApplicationOnlyJobResponse(Application application);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "appliedAt", ignore = true)
    Application formApplicationRequesttoApplication(ApplicationRequest source);

    @Mapping(target = "name", source = "fullName")
    @Mapping(target = "job", source = "job.title")
    RecentApplicationReponse fromApplicationtoRecentApplicationReponse(Application soure);

}
