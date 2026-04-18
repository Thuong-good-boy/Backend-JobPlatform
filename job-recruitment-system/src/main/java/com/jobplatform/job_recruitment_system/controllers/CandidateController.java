package com.jobplatform.job_recruitment_system.controllers;

import com.jobplatform.job_recruitment_system.dtos.Response.CandidateProfileResponse;
import com.jobplatform.job_recruitment_system.dtos.request.CandidateProfileRequest;
import com.jobplatform.job_recruitment_system.models.Candidate;
import com.jobplatform.job_recruitment_system.services.CandidateService;
import com.jobplatform.job_recruitment_system.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/candidate")
@RequiredArgsConstructor
public class CandidateController {

     private final CandidateService candidateService;
     private final UserService userService;
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
}