package com.jobplatform.job_recruitment_system.mapper;

import com.jobplatform.job_recruitment_system.dtos.Response.CandidateProfileResponse;
import com.jobplatform.job_recruitment_system.dtos.request.CandidateProfileRequest;
import com.jobplatform.job_recruitment_system.dtos.request.CandidateUpdateRequest;
import com.jobplatform.job_recruitment_system.models.Candidate;
import com.jobplatform.job_recruitment_system.models.User;
import org.mapstruct.*;

@Mapper(componentModel = "Spring")
public interface CandidateMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void upDidateCandidate(Candidate source, @MappingTarget Candidate taget);

    CandidateProfileResponse toDTO(User user, Candidate candidate);

    void upCadidateByDTO( CandidateProfileRequest source ,@MappingTarget Candidate taget);

    void upDateCandidateAdmin(CandidateUpdateRequest source, @MappingTarget Candidate taget);
}
