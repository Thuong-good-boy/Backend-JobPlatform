package com.jobplatform.job_recruitment_system.controllers;

import com.jobplatform.job_recruitment_system.dtos.DashboardDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/api/candidate")
public class DashboardController {
        @GetMapping("/dashboard-summary")
        public ResponseEntity<DashboardDTO> getSummary() {
            // Sau này bạn sẽ gọi Service để lấy từ DB
            DashboardDTO dto = new DashboardDTO();
            dto.setTotalApplied(5);
            dto.setTotalSaved(12);
            dto.setAverageMatchScore(85);
            return ResponseEntity.ok(dto);
        }

}
