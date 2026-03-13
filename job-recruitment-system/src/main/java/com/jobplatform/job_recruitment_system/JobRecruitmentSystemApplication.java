package com.jobplatform.job_recruitment_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class JobRecruitmentSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobRecruitmentSystemApplication.class, args);
	}

}
