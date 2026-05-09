package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.dtos.Response.SavedJobResponse;
import com.jobplatform.job_recruitment_system.exceptions.AppException;
import com.jobplatform.job_recruitment_system.exceptions.ErrorCode;
import com.jobplatform.job_recruitment_system.models.Company;
import com.jobplatform.job_recruitment_system.models.Job;
import com.jobplatform.job_recruitment_system.models.SavedJob;
import com.jobplatform.job_recruitment_system.models.User;
import com.jobplatform.job_recruitment_system.repositories.SavedJobRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@AllArgsConstructor
public class SaveJobService {

    private SavedJobRepository  savedJobRepository;
    private  UserService userService;
    private  JobService jobService;
    public Boolean existsByJobIdAndUserId(Long userId, Long jobId){
        return savedJobRepository.existsByJobIdAndUserId(jobId,userId);
    }
    public  void save(Long jobId){
        Long userId = userService.getCurrentUserId();
        boolean isAlreadySaved = existsByJobIdAndUserId( userId,jobId);
        if (isAlreadySaved) {
            new AppException(ErrorCode.JOB_008);
        }
        SavedJob newSavedJob = new SavedJob();
        User user = userService.getReferenceById(userId);
        Job job = jobService.getReferenceById(jobId);

        newSavedJob.setUser(user);
        newSavedJob.setJob(job);
        savedJobRepository.save(newSavedJob);
        return;
    }
    public void deleteByJobIdAndUserId(Long jobId, Long userId){
        savedJobRepository.deleteByJobIdAndUserId(jobId,userId);
        return;
    }
    public List<SavedJobResponse> getSavedJobsByUser(Long userId) {
        List<SavedJob> savedJobs = savedJobRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return savedJobs.stream().map(saved -> {
            Job job = saved.getJob();
            Company company = job.getCompany();

            SavedJobResponse dto = new SavedJobResponse();
            dto.setId(saved.getId());
            dto.setJobId(job.getId());
            dto.setJobTitle(job.getTitle());
            dto.setLocation(job.getLocation());

            if (job.getSalaryMin() != null && job.getSalaryMax() != null) {
                dto.setSalary(job.getSalaryMin() + " - " + job.getSalaryMax());
            } else {
                dto.setSalary("Thỏa thuận");
            }

           if (company != null) {
                dto.setCompanyName(company.getCompanyName());
                dto.setLogoUrl(company.getLogoUrl() != null ? company.getLogoUrl() : "https://ui-avatars.com/api/?name=" + company.getCompanyName());
            } else {
                dto.setCompanyName("Công ty bảo mật");
                dto.setLogoUrl("https://ui-avatars.com/api/?name=C");
            }

            if (saved.getCreatedAt() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                dto.setSavedDate(saved.getCreatedAt().format(formatter));
            } else {
                dto.setSavedDate("Vừa xong");
            }

            return dto;
        }).toList();
    }

}
