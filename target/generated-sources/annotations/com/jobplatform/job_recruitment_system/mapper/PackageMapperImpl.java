package com.jobplatform.job_recruitment_system.mapper;

import com.jobplatform.job_recruitment_system.dtos.Response.CandidateProResponse;
import com.jobplatform.job_recruitment_system.dtos.request.PackageRequest;
import com.jobplatform.job_recruitment_system.models.Package;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-10T18:55:51+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.6 (Oracle Corporation)"
)
@Component
public class PackageMapperImpl implements PackageMapper {

    @Override
    public CandidateProResponse formPackage(Package aPackage) {
        if ( aPackage == null ) {
            return null;
        }

        CandidateProResponse candidateProResponse = new CandidateProResponse();

        candidateProResponse.setId( aPackage.getId() );
        candidateProResponse.setPrice( aPackage.getPrice() );

        return candidateProResponse;
    }

    @Override
    public Package fromRequest(PackageRequest packageRequest) {
        if ( packageRequest == null ) {
            return null;
        }

        Package.PackageBuilder package1 = Package.builder();

        package1.name( packageRequest.getName() );
        package1.type( packageRequest.getType() );
        package1.price( packageRequest.getPrice() );
        package1.applyLimit( packageRequest.getApplyLimit() );
        package1.durationDays( packageRequest.getDurationDays() );
        package1.jobPostLimit( packageRequest.getJobPostLimit() );
        package1.status( packageRequest.getStatus() );

        return package1.build();
    }

    @Override
    public void updatePackage(Package aPackage, PackageRequest packageRequest) {
        if ( packageRequest == null ) {
            return;
        }

        aPackage.setName( packageRequest.getName() );
        aPackage.setType( packageRequest.getType() );
        aPackage.setPrice( packageRequest.getPrice() );
        aPackage.setApplyLimit( packageRequest.getApplyLimit() );
        aPackage.setDurationDays( packageRequest.getDurationDays() );
        aPackage.setJobPostLimit( packageRequest.getJobPostLimit() );
        aPackage.setStatus( packageRequest.getStatus() );
    }
}
