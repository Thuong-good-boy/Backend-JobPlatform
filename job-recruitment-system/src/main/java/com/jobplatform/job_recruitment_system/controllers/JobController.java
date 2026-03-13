package com.jobplatform.job_recruitment_system.controllers;

import com.jobplatform.job_recruitment_system.dtos.ApplicationRequestDTO;
import com.jobplatform.job_recruitment_system.dtos.JobPostRequest;
import com.jobplatform.job_recruitment_system.dtos.JobRecommendationDTO;
import com.jobplatform.job_recruitment_system.models.Application;
import com.jobplatform.job_recruitment_system.models.Job;
import com.jobplatform.job_recruitment_system.models.SavedJob;
import com.jobplatform.job_recruitment_system.models.User;
import com.jobplatform.job_recruitment_system.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    @Autowired
    private JobService jobService;
    @Autowired
    private AiMatchingService aiMatchingService;
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private SaveJobService saveJobService;
    @Autowired
    private UserService userService;


    @PostMapping("/create")
    public ResponseEntity<?> createJob(@RequestBody JobPostRequest request) {
        try {
            // Gọi service để xử lý (Lưu Job, lưu Skill, check user...)
            Job newJob = jobService.postJob(request);
            aiMatchingService.processNewJob(newJob);
            // Trả về Job vừa tạo thành công
            return ResponseEntity.ok(newJob);
        } catch (RuntimeException e) {
            // Bắt các lỗi logic (Ví dụ: Chưa tạo hồ sơ công ty)
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        } catch (Exception e) {
            // Lỗi hệ thống khác
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }
    @GetMapping
    public ResponseEntity<?> getAllJobs(@RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(jobService.getJobsForUser(userId));
    }
    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long userId
    ) {
        return ResponseEntity.ok(jobService.searchJobs(keyword, userId));
    }
    @GetMapping("/{jobId}")
    public  ResponseEntity<?> getJob(@PathVariable(required = true) Long jobId,
    @RequestParam(required = false) Long userId){
        return ResponseEntity.ok(jobService.getJobByjobIdandCvId(jobId,userId));

    }


    @PostMapping("/{jobId}/apply")
    public ResponseEntity<?> applyJob(@RequestBody ApplicationRequestDTO requestDTO) {
        try {
            Application result = applicationService.applyForJob(requestDTO);
            return ResponseEntity.ok("Ứng tuyển thành công!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/{jobId}/applied-jobs")
    public ResponseEntity<?> appliedJob(@PathVariable Long jobId,@RequestBody Map<String,Long> request){
        Long userId = request.get("userId");

            return ResponseEntity.ok( applicationService.checkByJobIdAndCv_User_Id(jobId,userId));
    }

    @PostMapping("/{jobId}/save")
    public ResponseEntity<?> saveJob(@PathVariable Long jobId, @RequestBody Map<String, Long> request) {
        Long userId= request.get("userId");
        boolean isAlreadySaved = saveJobService.existsByJobIdAndUserId( userId,jobId);

        if (isAlreadySaved) {
            return ResponseEntity.badRequest().body("Công việc này bạn đã lưu từ trước rồi!");
        }

        SavedJob newSavedJob = new SavedJob();

        User user = userService.getReferenceById(userId);
        Job job = jobService.getReferenceById(jobId);

        newSavedJob.setUser(user);
        newSavedJob.setJob(job);

        saveJobService.save(newSavedJob);

        return ResponseEntity.ok("Đã lưu công việc thành công!");
    }

    @DeleteMapping("/{jobId}/unsave")
    @Transactional
    public ResponseEntity<?> unsaveJob(@PathVariable Long jobId, @RequestBody Map<String,Long> request) {
        Long userId = request.get("userId");
        boolean isAlreadySaved = saveJobService.existsByJobIdAndUserId( userId, jobId);

        saveJobService.deleteByJobIdAndUserId(jobId, userId);

        return ResponseEntity.ok("Đã bỏ lưu công việc!");
    }

}
