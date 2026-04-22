package com.jobplatform.job_recruitment_system.controllers;

import com.jobplatform.job_recruitment_system.dtos.Response.*;
import com.jobplatform.job_recruitment_system.exceptions.AppException;
import com.jobplatform.job_recruitment_system.exceptions.ErrorCode;
import com.jobplatform.job_recruitment_system.services.AdminService;
import com.jobplatform.job_recruitment_system.services.AiMatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AiMatchingService aiMatchingService;
    private final AdminService adminService;

    @PostMapping("/sync-legacy-data")
    public ResponseEntity<?> syncAllOldData() {

        aiMatchingService.syncLegacyData();

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Lệnh đồng bộ đã được kích hoạt. Hệ thống đang chạy ngầm, vui lòng kiểm tra console log ở Backend để xem tiến độ!"
        ));
    }
    @GetMapping("Summary")
    public ResponseEntity<?> getSummary(){
        try {
            SummarryAdminReponse reponseSummary = adminService.getSummary();
            List<JobsLast3MonthsResponse>  jobsLast3MonthsResponses = adminService.getjobsLast3Months();
            List<CvsLast3MonthsResponse> cvsLast3MonthsResponses= adminService.getcvsLast3Months();
            List<RegistrationTrendsResponse> registrationTrendsResponses = adminService.getregistrationTrends();
            AccountDistribution distribution= adminService.getAccountDistribution();
            return  ResponseEntity.ok(Map.of("Summary",reponseSummary,
                    "accountDistribution",distribution,
                    "jobsLast3Months", jobsLast3MonthsResponses,
                    "cvsLast3Months",cvsLast3MonthsResponses,
                    "registrationTrends",registrationTrendsResponses));
        }catch (Exception e){
            e.printStackTrace();
             throw  new AppException(ErrorCode.ADMIN_1);

        }
    }

}