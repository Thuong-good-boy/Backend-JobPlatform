package com.jobplatform.job_recruitment_system.mapper;

import com.jobplatform.job_recruitment_system.dtos.Response.CandidateProResponse;
import com.jobplatform.job_recruitment_system.dtos.request.PackageRequest;
import com.jobplatform.job_recruitment_system.models.Package;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "Spring")
public interface PackageMapper {
    CandidateProResponse formPackage(Package aPackage);
    @Mapping(target = "cvViewLimit",source = "packageRequest.cvViewLimit")
    @Mapping(target = "pointsGranted",source = "packageRequest.pointsGranted")
    Package fromRequest(PackageRequest packageRequest);
    void updatePackage(@MappingTarget Package aPackage, PackageRequest packageRequest);
}
