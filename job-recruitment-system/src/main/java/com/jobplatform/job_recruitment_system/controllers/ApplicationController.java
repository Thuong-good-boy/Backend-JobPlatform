package com.jobplatform.job_recruitment_system.controllers;

import com.jobplatform.job_recruitment_system.dtos.ApplyRequest;
import com.jobplatform.job_recruitment_system.dtos.Response.ApplicationOnlyJobResponse;
import com.jobplatform.job_recruitment_system.models.*;
import com.jobplatform.job_recruitment_system.repositories.ApplicationRepository;
import com.jobplatform.job_recruitment_system.repositories.CvRepository;
import com.jobplatform.job_recruitment_system.repositories.JobRepository;
import com.jobplatform.job_recruitment_system.services.ApplicationService;
import com.jobplatform.job_recruitment_system.services.UserService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Validated
public class ApplicationController {
    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserService userService;
    private final CvRepository cvRepository; // Cần cái này để tìm CV
    private  final ApplicationService applicationService;
    @PostMapping("/apply")
    public ResponseEntity<?> applyJob(@RequestBody ApplyRequest request) {
            applicationService.applyJob(request);
            return ResponseEntity.ok("Ứng tuyển thành công!");
    }
    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<ApplicationOnlyJobResponse>> getApplicationsByJob(
            @PathVariable
                    @Min(value = 1, message = "JOB_INVALID")
            Long jobId,
            @RequestParam(defaultValue = "time")
            @Pattern(regexp = "^time|scores$", message = "SORT_BY_INVALID")
            String sortBy) {

        List<ApplicationOnlyJobResponse> response = applicationService.getApplicationsByJobId(jobId, sortBy);
        return ResponseEntity.ok(response);
    }
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable("id")
                    @Min(value = 1, message = "JOB_iNVALID")
            Long applicationId,

            @RequestParam("newStatus")
            @Pattern(regexp = "^APPLIED|VIEWED|INTERVIEW|REJECTED|ACCEPTED$" , message = "STATU_INALID")
            AppStatus newStatus) {
        try {
            applicationService.updateApplicationStatus(applicationId, newStatus);
            return ResponseEntity.ok("Đã cập nhật trạng thái thành " + newStatus);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }
}