package com.jobplatform.job_recruitment_system.mapper;

import com.jobplatform.job_recruitment_system.dtos.Response.CompanyDashboardResponse;
import com.jobplatform.job_recruitment_system.dtos.Response.CompanyProfileResponse;
import com.jobplatform.job_recruitment_system.dtos.request.CompanyOnboardingRequest;
import com.jobplatform.job_recruitment_system.dtos.request.CompanyUpdateRequest;
import com.jobplatform.job_recruitment_system.dtos.request.UpDateProfileCompanyRequest;
import com.jobplatform.job_recruitment_system.models.Company;
import com.jobplatform.job_recruitment_system.models.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-27T08:06:48+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.6 (Oracle Corporation)"
)
@Component
public class CompanyMapperImpl implements CompanyMapper {

    @Override
    public void upDateCompany(CompanyOnboardingRequest source, Company target) {
        if ( source == null ) {
            return;
        }

        target.setDescription( source.getDescription() );
    }

    @Override
    public CompanyDashboardResponse todto(Company company) {
        if ( company == null ) {
            return null;
        }

        CompanyDashboardResponse companyDashboardResponse = new CompanyDashboardResponse();

        companyDashboardResponse.setCompanyName( company.getCompanyName() );
        companyDashboardResponse.setLogoUrl( company.getLogoUrl() );
        companyDashboardResponse.setVerified( company.isVerified() );

        return companyDashboardResponse;
    }

    @Override
    public CompanyProfileResponse toProfileResponse(Company company) {
        if ( company == null ) {
            return null;
        }

        CompanyProfileResponse companyProfileResponse = new CompanyProfileResponse();

        companyProfileResponse.setEmail( companyUserEmail( company ) );
        companyProfileResponse.setRemainingCvViews( company.getRemainingCvViews() );
        companyProfileResponse.setCompanyName( company.getCompanyName() );
        companyProfileResponse.setTaxCode( company.getTaxCode() );
        companyProfileResponse.setWebsite( company.getWebsite() );
        companyProfileResponse.setAddress( company.getAddress() );
        companyProfileResponse.setDescription( company.getDescription() );
        companyProfileResponse.setLogoUrl( company.getLogoUrl() );
        companyProfileResponse.setVerified( company.isVerified() );
        companyProfileResponse.setRemainingBoosts( company.getRemainingBoosts() );

        return companyProfileResponse;
    }

    @Override
    public Company updateCompanyByProfileRequest(UpDateProfileCompanyRequest company, Company target) {
        if ( company == null ) {
            return target;
        }

        target.setWebsite( company.getWebsite() );
        target.setDescription( company.getDescription() );
        target.setAddress( company.getAddress() );

        return target;
    }

    @Override
    public void updateCompany(CompanyUpdateRequest request, Company company) {
        if ( request == null ) {
            return;
        }

        company.setCompanyName( request.getCompanyName() );
        company.setWebsite( request.getWebsite() );
        company.setDescription( request.getDescription() );
        company.setAddress( request.getAddress() );
        company.setTaxCode( request.getTaxCode() );
        company.setRemainingBoosts( request.getRemainingBoosts() );
    }

    private String companyUserEmail(Company company) {
        if ( company == null ) {
            return null;
        }
        User user = company.getUser();
        if ( user == null ) {
            return null;
        }
        String email = user.getEmail();
        if ( email == null ) {
            return null;
        }
        return email;
    }
}
