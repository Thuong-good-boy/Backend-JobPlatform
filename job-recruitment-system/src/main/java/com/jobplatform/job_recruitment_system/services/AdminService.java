package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.dtos.Response.*;
import com.jobplatform.job_recruitment_system.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final  UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private  final CandidateRepository candidateRepository;
    private  final  CvRepository cvRepository;
    public SummarryAdminReponse getSummary(){
        SummarryAdminReponse reponse = new SummarryAdminReponse();
        reponse.setTotalCandidate(candidateRepository.gettotalCandidate());
        reponse.setTotalApplications(applicationRepository.gettotalApplicationsAdmin());
        reponse.setTotalCompanies(companyRepository.totalCompany());
        reponse.setTotalJobs(jobRepository.totalActiveJobsAdmin());
        return  reponse;
    }
    public List<JobsLast3MonthsResponse> getjobsLast3Months(){
        return  jobRepository.getJobsLast3MonthsReponse();
    }
    public List<CvsLast3MonthsResponse> getcvsLast3Months(){
        return  cvRepository.getCvsLast3MonthsResponse();
    }
    public List<RegistrationTrendsResponse> getregistrationTrends(){
        return  userRepository.getRegistrationTrendsResponse();
    }
    public AccountDistribution getAccountDistribution(){
        AccountDistribution distribution = new AccountDistribution();
        distribution.setTotalCandidate(candidateRepository.gettotalCandidate());
        distribution.setTotalCompany(companyRepository.totalCompany());
        return  distribution;
    }

}
