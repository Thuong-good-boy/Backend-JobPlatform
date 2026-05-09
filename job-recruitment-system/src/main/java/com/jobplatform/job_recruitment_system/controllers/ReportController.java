package com.jobplatform.job_recruitment_system.controllers;

import com.jobplatform.job_recruitment_system.dtos.Response.ReportResponse;
import com.jobplatform.job_recruitment_system.dtos.request.ReportProcessRequest;
import com.jobplatform.job_recruitment_system.dtos.request.ReportSubmitRequest;
import com.jobplatform.job_recruitment_system.models.Report;
import com.jobplatform.job_recruitment_system.services.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.awt.print.Pageable;
import java.util.HashMap;
import java.util.Map;
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/report")
public class ReportController {
    private final ReportService reportService;
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createReport(@Valid @ModelAttribute ReportSubmitRequest request, BindingResult result){
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error -> {
                errors.put(error.getField(), error.getDefaultMessage());
            });

            return ResponseEntity.badRequest().body(errors);
        }
        try {
            reportService.creatReportToCompany(request);
            return ResponseEntity.ok("Tạo thành công!");
        }catch (Exception e){
            return ResponseEntity.ok("Tạo thất bại");

        }

    }
    @GetMapping
        public ResponseEntity<?> getReport(
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "10") int size
        ){
            Page<ReportResponse> reponse= reportService.getRepost(page,size);
            return ResponseEntity.ok(reponse);
        }
        @PutMapping("{reportId}/process")
        public  ResponseEntity<?> updateReport(@PathVariable("reportId") Long reportId, @RequestBody ReportProcessRequest request){
                return  ResponseEntity.ok(Map.of("message",reportService.updateStatusReport(reportId,request)));
        }
}
