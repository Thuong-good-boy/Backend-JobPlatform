package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.dtos.Response.ChartDataResponse;
import com.jobplatform.job_recruitment_system.dtos.Response.RecentApplicationReponse;
import com.jobplatform.job_recruitment_system.dtos.Response.SummarryAdminReponse;
import com.jobplatform.job_recruitment_system.dtos.Response.SummaryReponse;
import com.jobplatform.job_recruitment_system.exceptions.AppException;
import com.jobplatform.job_recruitment_system.exceptions.ErrorCode;
import com.jobplatform.job_recruitment_system.models.Application;
import com.jobplatform.job_recruitment_system.models.SavedJob;
import com.jobplatform.job_recruitment_system.repositories.ApplicationRepository;
import com.jobplatform.job_recruitment_system.repositories.CompanyRepository;
import com.jobplatform.job_recruitment_system.repositories.JobRepository;
import com.jobplatform.job_recruitment_system.repositories.SavedJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SummaryService {
    private  final JobRepository jobRepository;
    private  final ApplicationRepository applicationRepository;
    private final UserService userService;
    private  final SavedJobRepository savedJobRepository;
    private  final CompanyRepository companyRepository;
    public SummaryReponse getSumary(){
        try {
            SummaryReponse reponse = new SummaryReponse();
            Long userid = userService.getCurrentUserId();
            Long companyId = companyRepository.getCompanyId(userid);
            reponse.setTotalActiveJobs(jobRepository.totalActiveJobs(companyId));
            reponse.setTotalApplications(applicationRepository.totalApplications(companyId));
            reponse.setTotalSavedJobs(savedJobRepository.getTotalJobSave(companyId));
            return  reponse;
        }catch (Exception e){
            e.printStackTrace();

            throw new AppException(ErrorCode.USER_011);
        }

    }
    public List<ChartDataResponse> get7ngay(){
        Long userid =userService.getCurrentUserId();
        return  applicationRepository.totalApplicationsin7day(userid);
    }
    public  List<RecentApplicationReponse> getRecentApplicationReponse(){
        Long userid =userService.getCurrentUserId();
        return  applicationRepository.getTop5RecentApplications(userid);
    }

}
