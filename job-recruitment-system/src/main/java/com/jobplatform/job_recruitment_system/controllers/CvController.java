package com.jobplatform.job_recruitment_system.controllers;

import com.jobplatform.job_recruitment_system.models.Candidate;
import com.jobplatform.job_recruitment_system.models.Cv;

import com.jobplatform.job_recruitment_system.models.User;
import com.jobplatform.job_recruitment_system.services.*;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/cvs")
@RequiredArgsConstructor
public class CvController {

    private final CvService cvService;
    private final UserService userService;
    private  final CandidateService candidateService;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(
            @RequestParam("file")
                    @NotNull(message = "FILE_REQUIRED")
            MultipartFile file) throws  Exception {

            return ResponseEntity.ok(cvService.uploadAndAnalyze(file));

    }

    @GetMapping("/my-cvs")
    public ResponseEntity<?> getList() {
        List<Cv> cvList = cvService.getMyActiveCvs();
        Integer aiPoints = candidateService.getAiPoints();
        return ResponseEntity.ok(Map.of("aiPoints", aiPoints,"cvList", cvList));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
            cvService.softDelete(id);
            return ResponseEntity.ok(Map.of("message", "Đã xóa hồ sơ thành công"));
    }
}