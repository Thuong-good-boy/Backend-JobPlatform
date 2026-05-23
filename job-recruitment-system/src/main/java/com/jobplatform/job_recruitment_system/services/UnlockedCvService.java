package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.exceptions.AppException;
import com.jobplatform.job_recruitment_system.exceptions.ErrorCode;
import com.jobplatform.job_recruitment_system.models.Candidate;
import com.jobplatform.job_recruitment_system.models.Company;
import com.jobplatform.job_recruitment_system.models.UnlockedCv;
import com.jobplatform.job_recruitment_system.repositories.CandidateRepository;
import com.jobplatform.job_recruitment_system.repositories.CompanyRepository;
import com.jobplatform.job_recruitment_system.repositories.UnlockedCvRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UnlockedCvService {
    private  final UnlockedCvRepository repository;
    private  final UserService userService;
    private  final CompanyRepository companyRepository;
    private  final CandidateRepository candidateRepository;
    public  Boolean checkUnLockViewCandidate(Long candidateId){
        Long userId = userService.getCurrentUserId();
        if(userId == null){
            throw  new AppException(ErrorCode.AUTH_008);
        }
        Long companyId = companyRepository.getCompanyId(userId);
        return repository.existsByCompanyIdAndCandidateId( companyId, candidateId);
    }
    public  Integer unCLockCadiDate(Long candidateId){
        Long userId = userService.getCurrentUserId();
        if(userId == null){
            throw  new AppException(ErrorCode.AUTH_008);
        }
        Long companyId = companyRepository.getCompanyId(userId);
        Candidate candidate = candidateRepository.getReferenceById(candidateId);
        Company company = companyRepository.getReferenceById(companyId);
        if(company.getRemainingCvViews()<=0){
            throw  new AppException(ErrorCode.UNCLOCK_01);
        }else{
            company.setRemainingCvViews(company.getRemainingCvViews()-1);
            companyRepository.save(company);
            UnlockedCv  unlockedCv = new UnlockedCv();
            unlockedCv.setCandidate(candidate);
            unlockedCv.setCompany(company);
            repository.save(unlockedCv);
        }


        return company.getRemainingCvViews();
    }
}
