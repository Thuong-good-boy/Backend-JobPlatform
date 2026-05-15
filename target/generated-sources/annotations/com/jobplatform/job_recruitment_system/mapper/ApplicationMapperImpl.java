package com.jobplatform.job_recruitment_system.mapper;

import com.jobplatform.job_recruitment_system.dtos.Response.ApplicationOnlyJobResponse;
import com.jobplatform.job_recruitment_system.dtos.Response.RecentApplicationReponse;
import com.jobplatform.job_recruitment_system.dtos.request.ApplicationRequest;
import com.jobplatform.job_recruitment_system.models.Application;
import com.jobplatform.job_recruitment_system.models.Cv;
import com.jobplatform.job_recruitment_system.models.Job;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-16T05:57:45+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.6 (Oracle Corporation)"
)
@Component
public class ApplicationMapperImpl implements ApplicationMapper {

    private final DateTimeFormatter dateTimeFormatter_dd_MM_yyyy_HH_mm_0230740742 = DateTimeFormatter.ofPattern( "dd/MM/yyyy HH:mm" );

    @Override
    public ApplicationOnlyJobResponse toApplicationOnlyJobResponse(Application application) {
        if ( application == null ) {
            return null;
        }

        ApplicationOnlyJobResponse applicationOnlyJobResponse = new ApplicationOnlyJobResponse();

        if ( application.getAppliedAt() != null ) {
            applicationOnlyJobResponse.setAppliedAt( application.getAppliedAt() );
        }
        else {
            applicationOnlyJobResponse.setAppliedAt( LocalDateTime.parse( "Chưa cập nhật", dateTimeFormatter_dd_MM_yyyy_HH_mm_0230740742 ) );
        }
        applicationOnlyJobResponse.setCvId( applicationCvId( application ) );
        applicationOnlyJobResponse.setCvUrl( applicationCvFileUrl( application ) );
        applicationOnlyJobResponse.setId( application.getId() );
        applicationOnlyJobResponse.setPhone( application.getPhone() );
        applicationOnlyJobResponse.setAddress( application.getAddress() );
        applicationOnlyJobResponse.setStatus( application.getStatus() );
        applicationOnlyJobResponse.setCoverLetter( application.getCoverLetter() );

        return applicationOnlyJobResponse;
    }

    @Override
    public Application formApplicationRequesttoApplication(ApplicationRequest source) {
        if ( source == null ) {
            return null;
        }

        Application application = new Application();

        application.setCoverLetter( source.getCoverLetter() );
        application.setFullName( source.getFullName() );
        application.setPhone( source.getPhone() );
        application.setAddress( source.getAddress() );

        return application;
    }

    @Override
    public RecentApplicationReponse fromApplicationtoRecentApplicationReponse(Application soure) {
        if ( soure == null ) {
            return null;
        }

        RecentApplicationReponse recentApplicationReponse = new RecentApplicationReponse();

        recentApplicationReponse.setName( soure.getFullName() );
        recentApplicationReponse.setJob( soureJobTitle( soure ) );
        recentApplicationReponse.setId( soure.getId() );
        recentApplicationReponse.setAppliedAt( soure.getAppliedAt() );
        if ( soure.getStatus() != null ) {
            recentApplicationReponse.setStatus( soure.getStatus().name() );
        }

        return recentApplicationReponse;
    }

    private Long applicationCvId(Application application) {
        if ( application == null ) {
            return null;
        }
        Cv cv = application.getCv();
        if ( cv == null ) {
            return null;
        }
        Long id = cv.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String applicationCvFileUrl(Application application) {
        if ( application == null ) {
            return null;
        }
        Cv cv = application.getCv();
        if ( cv == null ) {
            return null;
        }
        String fileUrl = cv.getFileUrl();
        if ( fileUrl == null ) {
            return null;
        }
        return fileUrl;
    }

    private String soureJobTitle(Application application) {
        if ( application == null ) {
            return null;
        }
        Job job = application.getJob();
        if ( job == null ) {
            return null;
        }
        String title = job.getTitle();
        if ( title == null ) {
            return null;
        }
        return title;
    }
}
