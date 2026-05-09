package com.jobplatform.job_recruitment_system.mapper;

import com.jobplatform.job_recruitment_system.dtos.request.ReportReasonRequest;
import com.jobplatform.job_recruitment_system.models.ReportReasons;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
@Mapper(componentModel = "Spring")
public interface ReportReasonMapper
{
    ReportReasons fromRequest(ReportReasonRequest request);
    void update( @MappingTarget ReportReasons reasons, ReportReasonRequest request);
}
