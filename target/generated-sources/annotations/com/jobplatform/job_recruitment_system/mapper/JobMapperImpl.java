package com.jobplatform.job_recruitment_system.mapper;

import com.jobplatform.job_recruitment_system.dtos.request.JobPostRequest;
import com.jobplatform.job_recruitment_system.models.Job;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-18T16:07:23+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.6 (Oracle Corporation)"
)
@Component
public class JobMapperImpl implements JobMapper {

    @Override
    public Job fromJobPostRequest(JobPostRequest jobPostRequest) {
        if ( jobPostRequest == null ) {
            return null;
        }

        Job job = new Job();

        job.setTitle( jobPostRequest.getTitle() );
        job.setDescription( jobPostRequest.getDescription() );
        job.setSalaryMin( jobPostRequest.getSalaryMin() );
        job.setSalaryMax( jobPostRequest.getSalaryMax() );
        job.setLocation( jobPostRequest.getLocation() );

        return job;
    }

    @Override
    public void updateJob(JobPostRequest source, Job target) {
        if ( source == null ) {
            return;
        }

        target.setTitle( source.getTitle() );
        target.setDescription( source.getDescription() );
        target.setSalaryMin( source.getSalaryMin() );
        target.setSalaryMax( source.getSalaryMax() );
        target.setLocation( source.getLocation() );
    }
}
