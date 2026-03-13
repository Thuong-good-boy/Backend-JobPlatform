package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;

public class AccountCleanupService {
    @Autowired
    private UserRepository userRepository;
    @Scheduled(cron = "0 0 0 * * ?")
    public void removeUnverifiedAccounts() {
        LocalDateTime cutOffTime = LocalDateTime.now().minusHours(24);
        userRepository.deleteUnverifiedAccounts(cutOffTime);

    }
}
