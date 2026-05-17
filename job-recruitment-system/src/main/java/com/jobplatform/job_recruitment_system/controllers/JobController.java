package com.jobplatform.job_recruitment_system.controllers;

import com.jobplatform.job_recruitment_system.dtos.Response.*;
import com.jobplatform.job_recruitment_system.dtos.request.ApplicationRequest;
import com.jobplatform.job_recruitment_system.dtos.request.JobPostRequest;
import com.jobplatform.job_recruitment_system.enums.JobStatus;
import com.jobplatform.job_recruitment_system.exceptions.AppException;
import com.jobplatform.job_recruitment_system.exceptions.ErrorCode;
import com.jobplatform.job_recruitment_system.models.*;
import com.jobplatform.job_recruitment_system.services.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {
    private final JobService jobService;
    private final ApplicationService applicationService;
    private final SaveJobService saveJobService;
    private final UserService userService;
    private final  ReportReasonsService reasonsService;

    @PostMapping("/create")
    public ResponseEntity<?> createJob(@Valid @RequestBody JobPostRequest request) {
        jobService.postJob(request);
        return ResponseEntity.ok(Map.of("message", "đăng job thành công"));
    }
    @GetMapping("/jobpost")
    public ResponseEntity<?> getAllJobsPost(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size
    ) {
        Slice<Job> jobPro = jobService.getJobsPro(page,size);
        return ResponseEntity.ok( jobPro);
    }
    @GetMapping
    public ResponseEntity<?> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size
    ) {
        Slice<Job> job= jobService.getJobsForUser(page, size);
        return ResponseEntity.ok( job);
    }

    @GetMapping("/recommended")
    public ResponseEntity<?> getAllPRo(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size
    ) {
        Slice<JobRecommendationResponse> sliceJob = jobService.getJobRecommendationResponses(page, size);
        return ResponseEntity.ok(  sliceJob);
    }

    @GetMapping("/company/job")
    public ResponseEntity<?> getalljobforcompany(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(required = false) Long companyId) {
        Page<Job> list = jobService.getalljobforcompany(companyId,page, size);
        return ResponseEntity.ok(Map.of("data", list));

    }

    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size
    ) {
        return ResponseEntity.ok(jobService.searchJobs(keyword, page, size));
    }

    @GetMapping("/my-company-jobs")
    public ResponseEntity<?> getMyCompanyJobs() {
        try {
            List<ListJobResponse> myJobs = jobService.getJobsByCompanyUserId();
            return ResponseEntity.ok(myJobs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi lấy danh sách việc làm: " + e.getMessage());
        }
    }

    @PostMapping("/{jobId}/post")
    public ResponseEntity<?> postJob(@PathVariable @NotNull(message = "JOB_REQUIRED") Long jobId) {
        try {
            Integer countPost= jobService.postJob(jobId);
            return ResponseEntity.ok(countPost);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Bạn không còn lượt đẩy nào.");
        }
    }


    @PostMapping("/{jobId}/apply")
    public ResponseEntity<?> applyJob(@PathVariable @NotNull(message = "JOB_REQUIRED") Long jobId) {
        try {
             applicationService.applyForJob(jobId);
            return ResponseEntity.ok("Ứng tuyển thành công!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{jobId}/applied-jobs")
    public ResponseEntity<?> appliedJob(@PathVariable @NotNull(message = "JOB_REQUIRED") Long jobId) {
        try {
            Long userId = userService.getCurrentUserId();
            return ResponseEntity.ok(applicationService.checkByJobIdAndCv_User_Id(jobId, userId));
        }catch (Exception e){
            return ResponseEntity.ok(false);
        }

    }

    @GetMapping("/{jobId}/saved-jobs")
    public ResponseEntity<?> savedJob(@PathVariable @NotNull(message = "JOB_REQUIRED") Long jobId) {
        try {
        Long userId = userService.getCurrentUserId();
        return ResponseEntity.ok(saveJobService.existsByJobIdAndUserId(userId, jobId));
    }catch (Exception e){
        return ResponseEntity.ok(false);
    }
    }

    @PostMapping("/{jobId}/save")
    public ResponseEntity<?> saveJob(@PathVariable @NotNull(message = "JOB_REQUIRED") Long jobId) {
        saveJobService.save(jobId);
        return ResponseEntity.ok("Đã lưu công việc thành công!");
    }

    @DeleteMapping("/{jobId}/unsave")
    @Transactional
    public ResponseEntity<?> unsaveJob(@PathVariable @NotNull(message = "JOB_REQUIRED") Long jobId) {
        Long userId = userService.getCurrentUserId();
        saveJobService.deleteByJobIdAndUserId(jobId, userId);
        return ResponseEntity.ok("Đã bỏ lưu công việc!");
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<?> getjob(@PathVariable @NotNull(message = "JOB_REQUIRED") Long jobId) {
        List<ReportReasons> reportReasonsList = reasonsService.getReportJob();
        Job job = jobService.getJobsById(jobId).orElseThrow( ()-> new AppException(ErrorCode.JOB_001));
        return ResponseEntity.ok(Map.of("job", job, "reportReasonsList", reportReasonsList));
    }

    @GetMapping("/my-applications")
    public ResponseEntity<?> getMyApplications() {
        try {
            Long userId = userService.getCurrentUserId();
            List<AppliedJobResponse> result = applicationService.getAppliedJobsByUser(userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi khi lấy danh sách: " + e.getMessage());
        }
    }

    @GetMapping("/my-saved-jobs")
    public ResponseEntity<?> getMySavedJobs() {
        try {
            Long userId = userService.getCurrentUserId();
            List<SavedJobResponse> result = saveJobService.getSavedJobsByUser(userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi lấy danh sách việc đã lưu: " + e.getMessage());
        }
    }

    @PatchMapping("/{jobId}/status")
    public ResponseEntity<?> updateJobStatus(
            @PathVariable @NotNull(message = "JOB_REQUIRED") Long jobId,
            @RequestParam @NotNull(message = "STATUS_REQUIRED") JobStatus status
    ) {
        try {

            Job updatedJob = jobService.updateJobStatus(jobId, status);
            return ResponseEntity.ok(updatedJob);
        } catch (RuntimeException e) {
            return ResponseEntity.ok(Map.of("message",e.getMessage()));

        }
    }


    @PutMapping("/{jobId}")
    public ResponseEntity<?> updateJob(
            @PathVariable @NotNull(message = "JOB_REQUIRED") Long jobId
            , @Valid
            @RequestBody JobPostRequest request
    ) {

            Long userId = userService.getCurrentUserId();
            Job updatedJob = jobService.updateJob(jobId, request, userId);
            return ResponseEntity.ok(updatedJob);

    }

    @DeleteMapping("/{jobId}")
    public ResponseEntity<?> deleteJob(@PathVariable @NotNull(message = "JOB_REQUIRED") Long jobId) {
        try {
            Long userId = userService.getCurrentUserId();
            jobService.deleteJob(jobId, userId);
            return ResponseEntity.ok("Đã xóa tin tuyển dụng thành công!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

}
