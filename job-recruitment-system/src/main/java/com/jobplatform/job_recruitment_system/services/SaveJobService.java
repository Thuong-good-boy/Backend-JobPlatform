package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.models.SavedJob;
import com.jobplatform.job_recruitment_system.repositories.SavedJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SaveJobService {
    @Autowired
    private SavedJobRepository  jobRepository;
    public Boolean existsByJobIdAndUserId(Long userId, Long jobId){
        return jobRepository.existsByJobIdAndUserId(userId,jobId);
    }
    public  void save(SavedJob savedJob){
        jobRepository.save(savedJob);
        return;
    }
    public void deleteByJobIdAndUserId(Long jobId, Long userId){
        jobRepository.deleteByJobIdAndUserId(jobId,userId);
        return;
    }


}
