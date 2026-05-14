package com.jobplatform.job_recruitment_system.mapper;

import com.jobplatform.job_recruitment_system.dtos.request.ReportReasonRequest;
import com.jobplatform.job_recruitment_system.models.ReportReasons;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-10T18:55:50+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.6 (Oracle Corporation)"
)
@Component
public class ReportReasonMapperImpl implements ReportReasonMapper {

    @Override
    public ReportReasons fromRequest(ReportReasonRequest request) {
        if ( request == null ) {
            return null;
        }

        ReportReasons reportReasons = new ReportReasons();

        reportReasons.setTitle( request.getTitle() );
        reportReasons.setTargetType( request.getTargetType() );

        return reportReasons;
    }

    @Override
    public void update(ReportReasons reasons, ReportReasonRequest request) {
        if ( request == null ) {
            return;
        }

        reasons.setTitle( request.getTitle() );
        reasons.setTargetType( request.getTargetType() );
    }
}
