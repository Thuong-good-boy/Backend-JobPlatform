package com.jobplatform.job_recruitment_system.mapper;

import com.jobplatform.job_recruitment_system.dtos.Response.ListJobResponse;
import com.jobplatform.job_recruitment_system.dtos.request.JobPostRequest;
import com.jobplatform.job_recruitment_system.models.Job;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-16T05:57:45+0700",
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

    @Override
    public ListJobResponse fromJobtoListJobResponse(Job job) {
        if ( job == null ) {
            return null;
        }

        ListJobResponse listJobResponse = new ListJobResponse();

        listJobResponse.setId( job.getId() );
        listJobResponse.setTitle( job.getTitle() );
        listJobResponse.setLocation( job.getLocation() );
        if ( job.getSalaryMin() != null ) {
            listJobResponse.setSalaryMin( job.getSalaryMin().intValue() );
        }
        if ( job.getSalaryMax() != null ) {
            listJobResponse.setSalaryMax( job.getSalaryMax().intValue() );
        }
        if ( job.getStatus() != null ) {
            listJobResponse.setStatus( job.getStatus().name() );
        }
        listJobResponse.setCreatedAt( job.getCreatedAt() );
        listJobResponse.setSkills( maptoStrings( job.getSkills() ) );
        listJobResponse.setTrendingUntil( job.getTrendingUntil() );

        return listJobResponse;
    }
}
