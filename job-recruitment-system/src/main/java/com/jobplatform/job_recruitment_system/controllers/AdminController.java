package com.jobplatform.job_recruitment_system.controllers;

import com.jobplatform.job_recruitment_system.dtos.Response.*;
import com.jobplatform.job_recruitment_system.dtos.request.CandidateUpdateRequest;
import com.jobplatform.job_recruitment_system.dtos.request.CompanyUpdateRequest;
import com.jobplatform.job_recruitment_system.dtos.request.LoginRequest;
import com.jobplatform.job_recruitment_system.dtos.request.PackageRequest;
import com.jobplatform.job_recruitment_system.enums.Role;
import com.jobplatform.job_recruitment_system.exceptions.AppException;
import com.jobplatform.job_recruitment_system.exceptions.ErrorCode;
import com.jobplatform.job_recruitment_system.models.User;
import com.jobplatform.job_recruitment_system.services.AdminService;
import com.jobplatform.job_recruitment_system.services.AiMatchingService;
import com.jobplatform.job_recruitment_system.services.JobService;
import com.jobplatform.job_recruitment_system.services.UserService;
import com.jobplatform.job_recruitment_system.utils.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AiMatchingService aiMatchingService;
    private final AdminService adminService;
    private  final UserService userService;
    private  final JobService jobService;

    @PostMapping("/sync-legacy-data")
    public ResponseEntity<?> syncAllOldData() {

        aiMatchingService.syncLegacyData();

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Lệnh đồng bộ đã được kích hoạt. Hệ thống đang chạy ngầm, vui lòng kiểm tra console log ở Backend để xem tiến độ!"
        ));
    }
    @GetMapping("Summary")
    public ResponseEntity<?> getSummary(){
        try {
            SummarryAdminReponse reponseSummary = adminService.getSummary();
            List<JobsLast3MonthsResponse>  jobsLast3MonthsResponses = adminService.getjobsLast3Months();
            List<CvsLast3MonthsResponse> cvsLast3MonthsResponses= adminService.getcvsLast3Months();
            List<RegistrationTrendsResponse> registrationTrendsResponses = adminService.getregistrationTrends();
            AccountDistribution distribution= adminService.getAccountDistribution();
            return  ResponseEntity.ok(Map.of("Summary",reponseSummary,
                    "accountDistribution",distribution,
                    "jobsLast3Months", jobsLast3MonthsResponses,
                    "cvsLast3Months",cvsLast3MonthsResponses,
                    "registrationTrends",registrationTrendsResponses));
        }catch (Exception e){
            e.printStackTrace();
             throw  new AppException(ErrorCode.ADMIN_1);

        }
    }
    @GetMapping("/UserManagement")
    public ResponseEntity<?> getAdminUserResponse(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size,
                                                  @RequestParam(defaultValue = "CANDIDATE")  Role role, @RequestParam(defaultValue = "") String search){
        Pageable pageable = PageRequest.of(page,size);
        Page<AdminUserResponse> list = userService.getAdminUserResponse( pageable, role, search);
        return  ResponseEntity.ok(Map.of("data",list));
    }
    @PutMapping("/users/{id}")
    public  ResponseEntity<?> deleteUser(@PathVariable("id") Long id){
         userService.getUserId(id).orElseThrow(()-> new AppException(ErrorCode.AUTH_008));
        Boolean active = userService.isActive(id);
        if(Boolean.TRUE.equals(active)){
            userService.blockUser(id);
            return ResponseEntity.ok( Map.of("message" , "Đã khóa thành công."));
        }else{
            userService.unBlockUser(id);
            return ResponseEntity.ok( Map.of("message" , "Đã mở khóa thành công khóa thành công."));
        }

    }
    @PutMapping("/candidates/{id}")
    public ResponseEntity<?> updateCandidate(@PathVariable Long id, @Valid @RequestBody CandidateUpdateRequest request) {
        userService.updateCandidate(id, request);
        return ResponseEntity.ok(Map.of("message", "Cập nhật ứng viên thành công!"));
    }

    @PutMapping("/companies/{id}")
    public ResponseEntity<?> updateCompany(@PathVariable Long id, @Valid @RequestBody CompanyUpdateRequest request) {
        userService.updateCompany(id, request);
        return ResponseEntity.ok(Map.of("message", "Cập nhật doanh nghiệp thành công!"));
    }
        @GetMapping("/jobManagement")
    public ResponseEntity<?> getAllJobs(
            @RequestParam(defaultValue = "0")int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam("search") String search,
            @RequestParam("status") String status
    ) {
        return ResponseEntity.ok( jobService.getJobsForAdmin(page,size, search,status));
    }



}