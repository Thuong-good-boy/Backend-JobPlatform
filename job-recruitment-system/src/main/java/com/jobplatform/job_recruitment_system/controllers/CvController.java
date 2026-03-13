package com.jobplatform.job_recruitment_system.controllers;

import com.jobplatform.job_recruitment_system.models.Cv;
import com.jobplatform.job_recruitment_system.models.User;
import com.jobplatform.job_recruitment_system.repositories.CvRepository;
import com.jobplatform.job_recruitment_system.repositories.UserRepository;
import com.jobplatform.job_recruitment_system.services.AiMatchingService;
import com.jobplatform.job_recruitment_system.services.AiOcrService;
import com.jobplatform.job_recruitment_system.services.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/cvs")
@RequiredArgsConstructor
public class CvController {

    private final FileUploadService fileUploadService;
    private final AiOcrService aiOcrService;
    private final CvRepository cvRepository;
    private final UserRepository userRepository;
    private final AiMatchingService aiMatchingService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadAndAnalyzeCv(@RequestParam("file") MultipartFile file) {
        try {
            // 1. Lấy thông tin User hiện tại (Candidate)
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(email).orElseThrow();

            // 2. Đẩy file lên Cloudinary lấy URL
            String imageUrl = fileUploadService.uploadFile(file);

            // 3. Gọi AI để "đọc" nội dung CV
            String cvDataJson = aiOcrService.extractCvInfoToJson(file);

            // 4. Lưu vào Database
            Cv cv = new Cv();
            cv.setUser(user);
            cv.setFileUrl(imageUrl);
            cv.setCvData(cvDataJson); // Lưu chuỗi JSON vào cột jsonb

            cvRepository.save(cv);
            aiMatchingService.processNewCv(cv);

            return ResponseEntity.ok(Map.of(
                    "message", "Upload và phân tích CV thành công!",
                    "cvId", cv.getId(),
                    "analysis", cvDataJson
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi: " + e.getMessage());
        }
    }
}