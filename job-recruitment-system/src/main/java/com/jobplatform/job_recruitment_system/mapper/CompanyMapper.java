package com.jobplatform.job_recruitment_system.mapper;

import com.jobplatform.job_recruitment_system.dtos.Response.CompanyDashboardResponse;
import com.jobplatform.job_recruitment_system.dtos.Response.CompanyProfileResponse;
import com.jobplatform.job_recruitment_system.dtos.request.CompanyOnboardingRequest;
import com.jobplatform.job_recruitment_system.dtos.request.CompanyUpdateRequest;
import com.jobplatform.job_recruitment_system.dtos.request.UpDateProfileCompanyRequest;
import com.jobplatform.job_recruitment_system.models.Company;
import com.jobplatform.job_recruitment_system.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "Spring")
public interface CompanyMapper {
    @Mapping(target = "logoUrl", ignore = true)
    @Mapping(target = "licenseImageUrl", ignore = true)
    @Mapping(target = "remainingBoosts", ignore = true)
    void upDateCompany(CompanyOnboardingRequest source,@MappingTarget Company  target);

    @Mapping(target = "activeJobs", ignore = true)
    @Mapping(target = "totalApplications", ignore = true)
    @Mapping(target = "newApplications", ignore = true)
    CompanyDashboardResponse todto(Company company);

    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "proEnd", ignore = true)
    @Mapping(target = "remainingCvViews",source = "remainingCvViews")
    CompanyProfileResponse toProfileResponse(Company company);

    Company updateCompanyByProfileRequest(UpDateProfileCompanyRequest company, @MappingTarget Company target);
    void updateCompany(CompanyUpdateRequest request, @MappingTarget Company company);
}
