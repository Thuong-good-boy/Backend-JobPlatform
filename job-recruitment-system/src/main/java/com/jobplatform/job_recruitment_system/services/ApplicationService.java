package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.dtos.ApplicationRequestDTO;
import com.jobplatform.job_recruitment_system.models.AppStatus;
import com.jobplatform.job_recruitment_system.models.Application;
import com.jobplatform.job_recruitment_system.models.Cv;
import com.jobplatform.job_recruitment_system.models.Job;
import com.jobplatform.job_recruitment_system.repositories.ApplicationRepository;
import com.jobplatform.job_recruitment_system.repositories.CvRepository;
import com.jobplatform.job_recruitment_system.repositories.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicationService {
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired private CvRepository cvRepository;
    @Autowired private JobRepository jobRepository;

    @Transactional
    public Application applyForJob(ApplicationRequestDTO request) {
        // 2. Lấy Job và CV (Lúc này chắc chắn cvId truyền lên là hợp lệ rồi)
        Job job = jobRepository.getReferenceById(request.getJobId());
        Cv cv = cvRepository.findById(request.getCvId())
                .orElseThrow(() -> new RuntimeException("CV không tồn tại!"));

        Application app = new Application();
        app.setJob(job);
        app.setCv(cv);
        app.setFullname(request.getFullname());
        app.setPhone(request.getPhone());
        app.setAddress(request.getAddress());
        app.setCoverLetter(request.getCoverLetter());
        app.setStatus(AppStatus.APPLIED);

        return applicationRepository.save(app);
    }
    public  Boolean checkByJobIdAndCv_User_Id(Long jobId, Long userId){
        return applicationRepository.existsByJobIdAndCv_User_Id(jobId,userId);
    }

}
