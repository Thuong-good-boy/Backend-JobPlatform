package com.jobplatform.job_recruitment_system.controllers;

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

    @PostMapping("/upload")
    public ResponseEntity<?> upload(
            @RequestParam("file")
                    @NotNull(message = "FILE_REQUIRED")
            MultipartFile file) throws  Exception {

            return ResponseEntity.ok(cvService.uploadAndAnalyze(file));

    }

    @GetMapping("/my-cvs")
    public ResponseEntity<List<Cv>> getList() {
        return ResponseEntity.ok(cvService.getMyActiveCvs());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
            cvService.softDelete(id);
            return ResponseEntity.ok(Map.of("message", "Đã xóa hồ sơ thành công"));
    }
}