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
    public SummaryReponse getSumary(){
        try {
            SummaryReponse reponse = new SummaryReponse();
            Long userid = userService.getCurrentUserId();
            reponse.setTotalActiveJobs(jobRepository.totalActiveJobs(userid));
            reponse.setTotalApplications(applicationRepository.totalApplications(userid));
            reponse.setTotalSavedJobs(savedJobRepository.getTotalJobSave(userid));
            return  reponse;
        }catch (Exception e){
            new AppException(ErrorCode.USER_011);
        }
        return null;
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
