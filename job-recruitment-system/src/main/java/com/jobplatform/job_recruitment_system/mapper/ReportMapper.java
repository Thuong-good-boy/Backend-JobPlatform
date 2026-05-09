package com.jobplatform.job_recruitment_system.mapper;

import com.jobplatform.job_recruitment_system.dtos.Response.ReportResponse;
import com.jobplatform.job_recruitment_system.dtos.request.ReportSubmitRequest;
import com.jobplatform.job_recruitment_system.models.Report;
import com.jobplatform.job_recruitment_system.models.ReportReasons;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "Spring")
public interface ReportMapper {
    @Mapping(target = "reporterName", source = "report.reporter.fullName")
    @Mapping(target = "reporterEmail", source = "report.reporter.email")
    @Mapping(target = "reasonTitle", ignore = true)
    @Mapping(target = "evidenceImages",ignore = true)
    ReportResponse fromentity(Report report);
}
