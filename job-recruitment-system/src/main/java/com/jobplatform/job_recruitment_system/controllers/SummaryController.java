package com.jobplatform.job_recruitment_system.controllers;

import com.jobplatform.job_recruitment_system.dtos.Response.ChartDataResponse;
import com.jobplatform.job_recruitment_system.dtos.Response.RecentApplicationReponse;
import com.jobplatform.job_recruitment_system.dtos.Response.SummarryAdminReponse;
import com.jobplatform.job_recruitment_system.dtos.Response.SummaryReponse;
import com.jobplatform.job_recruitment_system.services.SummaryService;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapping;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/Summary")
@RequiredArgsConstructor
public class SummaryController {
    private final SummaryService summaryService;
    @GetMapping
    public ResponseEntity<?> getSummary(){
        SummaryReponse reponse = summaryService.getSumary();
        return  ResponseEntity.ok(Map.of("data",reponse, "message","Lấy dữ liệu thành công"));
    }
    @GetMapping("/7ngay")
    public  List<ChartDataResponse> setSummary7ngay(){
        return summaryService.get7ngay();
    }
    @GetMapping("/list_last")
    public  List<RecentApplicationReponse> getRecentApplicationReponse(){
            return  summaryService.getRecentApplicationReponse();
    }



}
