package com.jobplatform.job_recruitment_system.mapper;

import com.jobplatform.job_recruitment_system.dtos.Response.CandidateProfileResponse;
import com.jobplatform.job_recruitment_system.dtos.request.CandidateProfileRequest;
import com.jobplatform.job_recruitment_system.dtos.request.CandidateUpdateRequest;
import com.jobplatform.job_recruitment_system.models.Candidate;
import com.jobplatform.job_recruitment_system.models.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-23T10:42:54+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.6 (Oracle Corporation)"
)
@Component
public class CandidateMapperImpl implements CandidateMapper {

    @Override
    public void upDidateCandidate(Candidate source, Candidate taget) {
        if ( source == null ) {
            return;
        }

        if ( source.getId() != null ) {
            taget.setId( source.getId() );
        }
        if ( source.getUser() != null ) {
            taget.setUser( source.getUser() );
        }
        if ( source.getTitle() != null ) {
            taget.setTitle( source.getTitle() );
        }
        if ( source.getBio() != null ) {
            taget.setBio( source.getBio() );
        }
        if ( source.getExperienceYears() != null ) {
            taget.setExperienceYears( source.getExperienceYears() );
        }
        if ( source.getLocation() != null ) {
            taget.setLocation( source.getLocation() );
        }
        if ( source.getExpectedSalaryMin() != null ) {
            taget.setExpectedSalaryMin( source.getExpectedSalaryMin() );
        }
        if ( source.getExpectedSalaryMax() != null ) {
            taget.setExpectedSalaryMax( source.getExpectedSalaryMax() );
        }
        if ( source.getIsPublic() != null ) {
            taget.setIsPublic( source.getIsPublic() );
        }
        if ( source.getAiPoints() != null ) {
            taget.setAiPoints( source.getAiPoints() );
        }
        if ( source.getUpdatedAt() != null ) {
            taget.setUpdatedAt( source.getUpdatedAt() );
        }
    }

    @Override
    public CandidateProfileResponse toDTO(User user, Candidate candidate) {
        if ( user == null && candidate == null ) {
            return null;
        }

        CandidateProfileResponse candidateProfileResponse = new CandidateProfileResponse();

        if ( user != null ) {
            candidateProfileResponse.setFullName( user.getFullName() );
            candidateProfileResponse.setEmail( user.getEmail() );
            candidateProfileResponse.setAvatarUrl( user.getAvatarUrl() );
        }
        if ( candidate != null ) {
            candidateProfileResponse.setAiPoints( candidate.getAiPoints() );
            candidateProfileResponse.setTitle( candidate.getTitle() );
            candidateProfileResponse.setBio( candidate.getBio() );
            candidateProfileResponse.setExperienceYears( candidate.getExperienceYears() );
            candidateProfileResponse.setLocation( candidate.getLocation() );
            candidateProfileResponse.setExpectedSalaryMin( candidate.getExpectedSalaryMin() );
            candidateProfileResponse.setExpectedSalaryMax( candidate.getExpectedSalaryMax() );
            candidateProfileResponse.setIsPublic( candidate.getIsPublic() );
        }

        return candidateProfileResponse;
    }

    @Override
    public void upCadidateByDTO(CandidateProfileRequest source, Candidate taget) {
        if ( source == null ) {
            return;
        }

        taget.setTitle( source.getTitle() );
        taget.setBio( source.getBio() );
        taget.setExperienceYears( source.getExperienceYears() );
        taget.setLocation( source.getLocation() );
        taget.setExpectedSalaryMin( source.getExpectedSalaryMin() );
        taget.setExpectedSalaryMax( source.getExpectedSalaryMax() );
        taget.setIsPublic( source.getIsPublic() );
    }

    @Override
    public void upDateCandidateAdmin(CandidateUpdateRequest source, Candidate taget) {
        if ( source == null ) {
            return;
        }

        taget.setTitle( source.getTitle() );
        taget.setBio( source.getBio() );
        taget.setExperienceYears( source.getExperienceYears() );
        taget.setLocation( source.getLocation() );
        taget.setExpectedSalaryMin( source.getExpectedSalaryMin() );
        taget.setExpectedSalaryMax( source.getExpectedSalaryMax() );
        taget.setIsPublic( source.getIsPublic() );
    }
}
