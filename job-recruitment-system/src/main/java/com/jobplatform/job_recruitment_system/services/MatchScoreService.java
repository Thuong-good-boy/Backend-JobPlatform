package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.dtos.Response.JobRecommendationResponse;
import com.jobplatform.job_recruitment_system.exceptions.AppException;
import com.jobplatform.job_recruitment_system.exceptions.ErrorCode;
import com.jobplatform.job_recruitment_system.models.Candidate;
import com.jobplatform.job_recruitment_system.models.Company;
import com.jobplatform.job_recruitment_system.models.Cv;
import com.jobplatform.job_recruitment_system.models.User;
import com.jobplatform.job_recruitment_system.repositories.CandidateRepository;
import com.jobplatform.job_recruitment_system.repositories.CompanyRepository;
import com.jobplatform.job_recruitment_system.repositories.CvRepository;
import com.jobplatform.job_recruitment_system.repositories.MatchScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchScoreService {
    private  final MatchScoreRepository matchScoreRepository;
    private  final CvRepository repository;
    private  final UserService userService;
    private  final CompanyRepository companyRepository;
    private  final CandidateRepository candidateRepository;
    public List<JobRecommendationResponse> findRecommendedJobsByCvIdAndCompanyId(Long userId){
        Long UserIdCompany =  userService.getCurrentUserId();
        User user = candidateRepository.getUserByCandidate(userId);
        Long companyId = companyRepository.getCompanyId(UserIdCompany);
        System.out.println("id check: " + user.getId());
        Cv cv = repository.findFirstByUser_IdAndActiveTrueOrderByCreatedAtDesc(user.getId()).orElse(null);
        if(cv!= null ){
            System.out.println("id check: " + cv.getId()+ " id company: "+ companyId);
            return matchScoreRepository.findRecommendedJobsByCvIdAndCompanyId(cv.getId(),companyId);
        }else{
            return null;
        }
    }

}
