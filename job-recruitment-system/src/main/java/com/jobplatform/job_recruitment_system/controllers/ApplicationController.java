package com.jobplatform.job_recruitment_system.controllers;

import com.jobplatform.job_recruitment_system.dtos.ApplyRequest;
import com.jobplatform.job_recruitment_system.models.Application;
import com.jobplatform.job_recruitment_system.models.Cv;
import com.jobplatform.job_recruitment_system.models.Job;
import com.jobplatform.job_recruitment_system.models.User;
import com.jobplatform.job_recruitment_system.repositories.ApplicationRepository;
import com.jobplatform.job_recruitment_system.repositories.CvRepository;
import com.jobplatform.job_recruitment_system.repositories.JobRepository;
import com.jobplatform.job_recruitment_system.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository; // Cần cái này để tìm User

    @Autowired
    private CvRepository cvRepository; // Cần cái này để tìm CV

    @PostMapping("/apply")
    public ResponseEntity<?> applyJob(@RequestBody ApplyRequest request) {
        try {
            // 1. Lấy email người dùng đang đăng nhập
            String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();

            // 2. Tìm User trong DB
            User candidate = userRepository.findByEmail(currentEmail)
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

            // 3. Tìm Job
            Job job = jobRepository.findById(request.getJobId())
                    .orElseThrow(() -> new RuntimeException("Công việc không tồn tại"));

            // 4. Tìm CV
            Cv cv = cvRepository.findById(request.getCvId())
                    .orElseThrow(() -> new RuntimeException("CV không tồn tại"));

            //  LOGIC QUAN TRỌNG: Kiểm tra quyền sở hữu CV
            // Nếu User của CV khác với User đang đăng nhập -> Báo lỗi
            if (!cv.getUser().getId().equals(candidate.getId())) {
                return ResponseEntity.status(403).body("Bạn không có quyền sử dụng CV này!");
            }

            // 5. Kiểm tra đã nộp chưa (Tránh spam)
            if (applicationRepository.existsByJobIdAndCvId(request.getJobId(), request.getCvId())) {
                return ResponseEntity.badRequest().body("Bạn đã ứng tuyển công việc này rồi!");
            }

            // 6. Lưu Application
            Application app = new Application();
            app.setJob(job);
            app.setCv(cv); // Set CV thật
            app.setCoverLetter(request.getCoverLetter());

            applicationRepository.save(app);

            return ResponseEntity.ok("Ứng tuyển thành công!");

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }
}