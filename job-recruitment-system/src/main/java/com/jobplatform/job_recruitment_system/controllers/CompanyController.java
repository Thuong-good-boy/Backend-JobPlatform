package com.jobplatform.job_recruitment_system.controllers;

import com.jobplatform.job_recruitment_system.dtos.Response.CompanyDashboardResponse;
import com.jobplatform.job_recruitment_system.dtos.Response.CompanyProfileResponse;
import com.jobplatform.job_recruitment_system.dtos.TopCompanyResponseDTO;
import com.jobplatform.job_recruitment_system.dtos.request.CompanyOnboardingRequest;
import com.jobplatform.job_recruitment_system.dtos.request.UpDateProfileCompanyRequest;
import com.jobplatform.job_recruitment_system.services.CompanyService;
import com.jobplatform.job_recruitment_system.services.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
public class CompanyController {


    private final CompanyService companyService;
     private final UserService userService;
    @PostMapping(value = "/onboarding", consumes = {"multipart/form-data"})
    public ResponseEntity<?> onboardingCompany(
           @Valid @ModelAttribute CompanyOnboardingRequest request) throws  Exception{
            companyService.processOnboarding(request);
            return ResponseEntity.ok("Cập nhật hồ sơ công ty thành công!");
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
    public ResponseEntity<List<TopCompanyResponseDTO>> getTopCompanies() {
        return ResponseEntity.ok(companyService.getTopCompanies());
    }

    @GetMapping("/search")
    public ResponseEntity<List<TopCompanyResponseDTO>> searchCompanies(@RequestParam("keyword") String keyword) {
        return ResponseEntity.ok(companyService.searchCompanies(keyword));
    }



}