package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.dtos.Response.JobRecommendationResponse;
import com.jobplatform.job_recruitment_system.repositories.MatchScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchScoreService {
    private  final MatchScoreRepository matchScoreRepository;
    public List<JobRecommendationResponse> findRecommendedJobsByCvIdAndCompanyId(Long userId, Long companyId){
         return matchScoreRepository.findRecommendedJobsByCvIdAndCompanyId(userId,companyId);
    }
}
