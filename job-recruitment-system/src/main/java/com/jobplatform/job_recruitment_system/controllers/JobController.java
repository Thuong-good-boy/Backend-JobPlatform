package com.jobplatform.job_recruitment_system.controllers;

import com.jobplatform.job_recruitment_system.dtos.Response.AppliedJobResponse;
import com.jobplatform.job_recruitment_system.dtos.Response.CompanyJobsByCandidateResponse;
import com.jobplatform.job_recruitment_system.dtos.Response.ListJobResponse;
import com.jobplatform.job_recruitment_system.dtos.Response.SavedJobResponse;
import com.jobplatform.job_recruitment_system.dtos.request.ApplicationRequest;
import com.jobplatform.job_recruitment_system.dtos.request.JobPostRequest;
import com.jobplatform.job_recruitment_system.enums.JobStatus;
import com.jobplatform.job_recruitment_system.models.*;
import com.jobplatform.job_recruitment_system.services.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
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
    private final AiMatchingService aiMatchingService;
    private final ApplicationService applicationService;
    private final SaveJobService saveJobService;
    private final UserService userService;
    private final CvService cvService;
    private final MatchScoreService matchScoreService;

    @PostMapping("/create")
    public ResponseEntity<?> createJob(@Valid @RequestBody JobPostRequest request) {
        jobService.postJob(request);
        return ResponseEntity.ok(Map.of("message", "đăng job thành công"));
    }

    @GetMapping
    public ResponseEntity<?> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(jobService.getJobsForUser(page, size));
    }

    @GetMapping("/recommended")
    public ResponseEntity<?> getAllPRo(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok((jobService.getJobRecommendationResponses(page, size)));
    }

    @GetMapping("/company/job")
    public ResponseEntity<?> getalljobforcompany(@RequestParam(required = false) Long companyId) {
        List<CompanyJobsByCandidateResponse> list = jobService.getalljobforcompany(companyId);
        return ResponseEntity.ok(Map.of("data", list));

    }

    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(jobService.searchJobs(keyword, page, size));
    }

    @GetMapping("/my-company-jobs")
    public ResponseEntity<?> getMyCompanyJobs() {
        try {
            Long userId = userService.getCurrentUserId();
            List<ListJobResponse> myJobs = jobService.getJobsByCompanyUserId(userId);
            return ResponseEntity.ok(myJobs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi lấy danh sách việc làm: " + e.getMessage());
        }
    }

    @PostMapping("/{jobId}/post")
    public ResponseEntity<?> postJob(@PathVariable @NotNull(message = "JOB_REQUIRED") Long jobId) {
        try {
            jobService.postJob(jobId);
            return ResponseEntity.ok("Đẩy job thành công !");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi đẩy tin: " + e.getMessage());
        }
    }


    @PostMapping("/{jobId}/apply")
    public ResponseEntity<?> applyJob(@PathVariable @NotNull(message = "JOB_REQUIRED") Long jobId, @Valid @RequestBody ApplicationRequest requestDTO) {
        try {
            Application result = applicationService.applyForJob(requestDTO, jobId);
            return ResponseEntity.ok("Ứng tuyển thành công!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{jobId}/applied-jobs")
    public ResponseEntity<?> appliedJob(@PathVariable @NotNull(message = "JOB_REQUIRED") Long jobId) {
        Long userId = userService.getCurrentUserId();
        return ResponseEntity.ok(applicationService.checkByJobIdAndCv_User_Id(jobId, userId));
    }

    @GetMapping("/{jobId}/saved-jobs")
    public ResponseEntity<?> savedJob(@PathVariable @NotNull(message = "JOB_REQUIRED") Long jobId) {
        Long userId = userService.getCurrentUserId();
        return ResponseEntity.ok(saveJobService.existsByJobIdAndUserId(userId, jobId));
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
    public ResponseEntity<Job> getjob(@PathVariable @NotNull(message = "JOB_REQUIRED") Long jobId) {
        return jobService.getJobsById(jobId).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
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
