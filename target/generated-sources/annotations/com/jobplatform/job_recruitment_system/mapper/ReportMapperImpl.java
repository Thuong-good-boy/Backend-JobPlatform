package com.jobplatform.job_recruitment_system.mapper;

import com.jobplatform.job_recruitment_system.dtos.Response.ReportResponse;
import com.jobplatform.job_recruitment_system.models.Report;
import com.jobplatform.job_recruitment_system.models.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-16T22:14:03+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.6 (Oracle Corporation)"
)
@Component
public class ReportMapperImpl implements ReportMapper {

    @Override
    public ReportResponse fromentity(Report report) {
        if ( report == null ) {
            return null;
        }

        ReportResponse reportResponse = new ReportResponse();

        reportResponse.setReporterName( reportReporterFullName( report ) );
        reportResponse.setReporterEmail( reportReporterEmail( report ) );
        reportResponse.setId( report.getId() );
        reportResponse.setTargetId( report.getTargetId() );
        reportResponse.setTargetType( report.getTargetType() );
        reportResponse.setDescription( report.getDescription() );
        reportResponse.setStatus( report.getStatus() );
        reportResponse.setAdminNote( report.getAdminNote() );
        reportResponse.setCreatedAt( report.getCreatedAt() );

        return reportResponse;
    }

    private String reportReporterFullName(Report report) {
        if ( report == null ) {
            return null;
        }
        User reporter = report.getReporter();
        if ( reporter == null ) {
            return null;
        }
        String fullName = reporter.getFullName();
        if ( fullName == null ) {
            return null;
        }
        return fullName;
    }

    private String reportReporterEmail(Report report) {
        if ( report == null ) {
            return null;
        }
        User reporter = report.getReporter();
        if ( reporter == null ) {
            return null;
        }
        String email = reporter.getEmail();
        if ( email == null ) {
            return null;
        }
        return email;
    }
}
