package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.repositories.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class JobSchedulerService {
    private  final JobRepository jobRepository;
    @Scheduled(cron = "0 0 2 * * ?")
    public  void nightlyCleanup(){
        try {
            int updatedCount = jobRepository.resetExpiredTrendingJobs();
            if (updatedCount > 0) {
                log.info(" Đã dọn dẹp {} tin hết hạn vào lúc 2h sáng", updatedCount);
            }
        } catch (Exception e) {
            log.error(" Lỗi khi dọn dẹp tin hết hạn: ", e);
        }
    }
}
