package com.jobplatform.job_recruitment_system.controllers;

import com.jobplatform.job_recruitment_system.dtos.request.ReportReasonRequest;
import com.jobplatform.job_recruitment_system.dtos.request.ReportSubmitRequest;
import com.jobplatform.job_recruitment_system.models.ReportReasons;
import com.jobplatform.job_recruitment_system.services.ReportReasonsService;
import com.jobplatform.job_recruitment_system.services.ReportService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reportreason")
@RequiredArgsConstructor

public class ReportReasonsController {
    private  final ReportReasonsService reasonsService;
    private  final ReportService reportService;

    @GetMapping
    public Page<ReportReasons> getReportReasons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return reasonsService.getReportReasons(page,size);
    }
    @PostMapping
    public ResponseEntity<?> createReport(@Valid @RequestBody ReportReasonRequest reportReasonRequest){
        reasonsService.createRepost(reportReasonRequest);
        return ResponseEntity.ok(Map.of("message","Đã thêm thành công."));
    }
    @PatchMapping("/{id}/updatestatus")
    public  ResponseEntity<?> updateStatus(@PathVariable @NotNull(message = "REPORT_REQUIRED") Long id){
        reasonsService.updateStatus(id);
        return ResponseEntity.ok(Map.of("message","Đã cập nhật thành công."));
    }
    @PutMapping("/{id}")
    public  ResponseEntity<?> updateReport(@PathVariable @NotNull(message = "REPORT_REQUIRED") Long id ,@RequestBody ReportReasonRequest request){
        reasonsService.updateReport(id,request);
        return ResponseEntity.ok(Map.of("message","Đã cập nhật thành công."));
    }


}
