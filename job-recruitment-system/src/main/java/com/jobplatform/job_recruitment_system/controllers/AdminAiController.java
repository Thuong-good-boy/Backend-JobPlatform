package com.jobplatform.job_recruitment_system.controllers;

import com.jobplatform.job_recruitment_system.services.AiMatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/ai")
@RequiredArgsConstructor
public class AdminAiController {

    private final AiMatchingService aiMatchingService;

    @PostMapping("/sync-legacy-data")
    public ResponseEntity<?> syncAllOldData() {

        aiMatchingService.syncLegacyData();

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Lệnh đồng bộ đã được kích hoạt. Hệ thống đang chạy ngầm, vui lòng kiểm tra console log ở Backend để xem tiến độ!"
        ));
    }
}