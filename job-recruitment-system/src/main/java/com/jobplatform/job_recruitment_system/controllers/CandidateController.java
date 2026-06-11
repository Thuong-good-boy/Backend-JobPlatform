package com.jobplatform.job_recruitment_system.controllers;

import com.jobplatform.job_recruitment_system.dtos.Response.CandidateProfileResponse;
import com.jobplatform.job_recruitment_system.dtos.Response.CompanyJobsByCandidateResponse;
import com.jobplatform.job_recruitment_system.dtos.request.CandidateProfileRequest;
import com.jobplatform.job_recruitment_system.dtos.request.ReportSubmitRequest;
import com.jobplatform.job_recruitment_system.models.Candidate;
import com.jobplatform.job_recruitment_system.models.Company;
import com.jobplatform.job_recruitment_system.models.Job;
import com.jobplatform.job_recruitment_system.models.ReportReasons;
import com.jobplatform.job_recruitment_system.repositories.CandidateRepository;
import com.jobplatform.job_recruitment_system.services.*;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/candidate")
@RequiredArgsConstructor
public class CandidateController {

     private final CandidateService candidateService;
     private final UserService userService;
     private final JobService jobService;
     private  final CompanyService companyService;
     private  final ReportReasonsService reasonsService;
    private  final  AiOcrService aiOcrService;
    private  final CandidateRepository candidateRepository;
    private  final CvService cvService;
    @PostMapping("/profile")
    public ResponseEntity<?> updateProfile( @RequestBody Candidate profile) {
        try {
            Long userId = userService.getCurrentUserId();
            Candidate updatedProfile = candidateService.saveOrUpdateProfile(userId, profile);
            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        Long userId = userService.getCurrentUserId();
        return ResponseEntity.ok(candidateService.getProfile(userId));
    }
    @GetMapping("/my-profile")
    public ResponseEntity<?> getMyProfile() {
        CandidateProfileResponse dto = candidateService.getMyProfile();
        return ResponseEntity.ok(dto);
    }
    @PostMapping("/my-profile/update")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody CandidateProfileRequest dto) {
        candidateService.updateProfile( dto);
        return ResponseEntity.ok("Cập nhật hồ sơ thành công!");
    }
    @GetMapping("/company/job")
    public ResponseEntity<?> getalljobandcompany(@RequestParam(required = false) Long companyId,
         @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "6") int size
    ) {
        System.out.println("company o: "+  companyId);
        Page<Job> list = jobService.getalljobforcompany(companyId, page, size);
        Company company = companyService.getCompanyById(companyId);
        return ResponseEntity.ok(Map.of(
                "data", list,
                "company",company));

    }
    @PostMapping("/update-logo")
    public  void updateLogo(
            @RequestParam("avatar") MultipartFile formData
    ){
        candidateService.postLogo(formData);
    }

    @PostMapping("/evaluate-cv-id")
    public ResponseEntity<?> evaluateCvById(@RequestBody Map<String, Object> requestData) {

        if (!requestData.containsKey("cvId") || requestData.get("cvId") == null) {
            return ResponseEntity.badRequest().body("ID CV không được để trống!");
        }

        try {
            Long cvId = Long.valueOf(requestData.get("cvId").toString());

            String feedbackJson = cvService.feedBack(cvId);
            return ResponseEntity.ok(feedbackJson);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Đã xảy ra lỗi khi phân tích CV: " + e.getMessage());
        }
    }


}