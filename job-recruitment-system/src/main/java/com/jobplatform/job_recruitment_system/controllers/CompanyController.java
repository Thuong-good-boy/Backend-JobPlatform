package com.jobplatform.job_recruitment_system.controllers;

import com.jobplatform.job_recruitment_system.dtos.Response.*;
import com.jobplatform.job_recruitment_system.dtos.request.CompanyOnboardingRequest;
import com.jobplatform.job_recruitment_system.dtos.request.ReportSubmitRequest;
import com.jobplatform.job_recruitment_system.dtos.request.UpDateProfileCompanyRequest;
import com.jobplatform.job_recruitment_system.models.Company;
import com.jobplatform.job_recruitment_system.models.ReportReasons;
import com.jobplatform.job_recruitment_system.services.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
public class CompanyController {


    private final CompanyService companyService;
    private  final CandidateService candidateService;
    private  final ReportReasonsService reasonsService;



    @PostMapping(value = "/onboarding", consumes = {"multipart/form-data"})
    public ResponseEntity<?> onboardingCompany(
           @Valid @ModelAttribute CompanyOnboardingRequest request) throws  Exception{
            companyService.processOnboarding(request);
            return ResponseEntity.ok("Cập nhật hồ sơ công ty thành công!");
    }
    @PostMapping("/update-logo")
    public  void updateLogo(
        @RequestParam("logo") MultipartFile formData
    ){
        companyService.postLogo(formData);
    }
    @GetMapping("/dashboard")
    public ResponseEntity<CompanyDashboardResponse> getDashboard() {
        CompanyDashboardResponse dashboard = companyService.getCompanyDashboard();
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/profile")
    public ResponseEntity<CompanyProfileResponse> getProfile() {
        CompanyProfileResponse profile = companyService.getCompanyProfile();
        return ResponseEntity.ok(profile);
    }
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UpDateProfileCompanyRequest requestData) {
            companyService.updateProfileText( requestData);
            return ResponseEntity.ok("Cập nhật thông tin thành công!");
    }

    @PostMapping(value = "/verify-license", consumes = {"multipart/form-data"})
    public ResponseEntity<?> verifyLicense(@RequestParam("licenseImage")
                                                @NotNull (message = "IMAGE_REQUIRED")
                                               MultipartFile licenseImage) throws  Exception {
            companyService.verifyLicense(licenseImage);
            return ResponseEntity.ok("Đã tải lên giấy phép, hệ thống đang chờ AI quét và duyệt!");
    }
    @GetMapping("/top-hiring")
    public ResponseEntity<List<TopCompanyResponse>> getTopCompanies() {
        return ResponseEntity.ok(companyService.getTopCompanies());
    }

    @GetMapping("/search")
    public ResponseEntity<List<TopCompanyResponse>> searchCompanies(@RequestParam("keyword") String keyword) {
        return ResponseEntity.ok(companyService.searchCompanies(keyword));
    }
    @GetMapping("/candidate/search")
    public  ResponseEntity<?> searchCandites(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer minExp,
            @RequestParam(required = false) List<String> skills,
            @RequestParam(defaultValue = "0") int page
    ){
        return ResponseEntity.ok(Map.of("data", companyService.searchCandidate(keyword, location, minExp, skills, page)));
    }
    @PostMapping("/candidate/profile")
    public ResponseEntity<?> getCandidateForCompany(
            @RequestBody Map<String, Long> request
    ) {
        Long id= request.get("id");
        List<ReportReasons> reportReasonsList = reasonsService.getReportCandidate();
        CandidateProfileResponse response = candidateService.getCandidateForcompany(id);
        return ResponseEntity.ok(
                Map.of("data",response, "reportReason",reportReasonsList)
        );
    }



}