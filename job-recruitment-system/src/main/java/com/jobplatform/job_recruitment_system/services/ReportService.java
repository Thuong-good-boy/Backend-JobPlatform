package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.config.CloudinaryConfig;
import com.jobplatform.job_recruitment_system.dtos.Response.ReportResponse;
import com.jobplatform.job_recruitment_system.dtos.request.ReportProcessRequest;
import com.jobplatform.job_recruitment_system.dtos.request.ReportSubmitRequest;
import com.jobplatform.job_recruitment_system.enums.ReportStatus;
import com.jobplatform.job_recruitment_system.exceptions.AppException;
import com.jobplatform.job_recruitment_system.exceptions.ErrorCode;
import com.jobplatform.job_recruitment_system.mapper.ReportMapper;
import com.jobplatform.job_recruitment_system.models.*;
import com.jobplatform.job_recruitment_system.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ReportService {
    private  final ReportRepository repository;
    private  final ReportEvidenceRepository reportEvidenceRepository;
    private  final ReportReasonsRepository reportReasonsRepository;
    private  final EmailService emailService;
    private  final UserService userService;
    private  final FileUploadService fileUploadService;
    private  final ReportMapper mapper;
    private  final  UserRepository userRepository;
    private  final CandidateRepository candidateRepository;
    private  final  CompanyRepository companyRepository;
    public  void creatReportToCompany(ReportSubmitRequest request) throws  Exception{
        Report report = new Report();
        User user = userService.getUserId(userService.getCurrentUserId()).orElseThrow(()-> new AppException(ErrorCode.AUTH_008));
        ReportReasons reasons = reportReasonsRepository.findById(request.getReasonId()).orElseThrow(()-> new AppException(ErrorCode.REPORTREASON_01));

        report.setReporter(user);
        report.setReportReason(reasons);
        report.setTargetId(request.getTargetId());
        report.setDescription(request.getDescription());
        report.setStatus(ReportStatus.PENDING);
        repository.save(report);
        List<ReportEvidence> list = new ArrayList<>();

        if(request.getFiles()!= null && !request.getFiles().isEmpty()){
            for(MultipartFile file: request.getFiles()){
                String imageUrl = fileUploadService.uploadFile(file);
                ReportEvidence reportEvidence = new ReportEvidence();
                reportEvidence.setImageUrl(imageUrl);
                reportEvidence.setReport(report);
                ReportEvidence response= reportEvidenceRepository.save(reportEvidence);
                list.add(response);
            }
        }
        report.setEvidences(list);
        repository.save(report);
    }
    public Page<ReportResponse> getRepost(int page, int size){
        Pageable pageable = PageRequest.of(page,size);
        Page<Report> reportPage = repository.findAll(pageable);
        Page<ReportResponse> reportResponses = reportPage.map(
                report -> {
                    ReportResponse dto = mapper.fromentity(report);
                    if(report.getReportReason() != null){
                        dto.setReasonTitle(report.getReportReason().getTitle());
                    }
                    List<String> images= report.getEvidences().stream().map(
                            ReportEvidence :: getImageUrl
                    ).toList();
                    dto.setEvidenceImages(images);
                    return  dto;
                }
        );
        return  reportResponses;
    }
    public  String updateStatusReport( Long id,ReportProcessRequest request){
        Report report = repository.findById(id).orElseThrow(()-> new AppException(ErrorCode.REPORT_01));
        report.setStatus(request.getStatus());
        report.setAdminNote(request.getAdminNote());
        repository.save(report);
        String targetTypeStr = (report.getReportReason().getTargetType().name().equals("CANDIDATE"))
                ? "Ứng viên" : "Nhà Tuyển Dụng";
        try {
            emailService.sendReportFeedbackEmail(
                    report.getReporter().getEmail(),
                    report.getReporter().getFullName(),
                    report.getId(),
                    targetTypeStr,
                    report.getReportReason().getTitle(),
                    request.getStatus(),
                    request.getAdminNote()
            );
        } catch (Exception e) {
            System.err.println("Lỗi gửi mail phản hồi báo cáo: " + e.getMessage());
        }

        if (request.getStatus() == ReportStatus.RESOLVED) {
                if (targetTypeStr.equals("Ứng viên")){
                    User user  = candidateRepository.getUserByCandidate(report.getTargetId());
                    user.setActive(false);
                    userRepository.save(user);
                }else{
                    User user = userService.getReferenceById(report.getTargetId());
                    user.setActive(false);
                    userRepository.save(user);
                }
            return "Đã XÁC NHẬN vi phạm, xử lý tài khoản & gửi email cho người tố cáo!";

        } else if (request.getStatus() == ReportStatus.REJECTED) {
            return "Đã TỪ CHỐI báo cáo & gửi email giải thích cho người tố cáo!";

        } else {

            return "Đã cập nhật trạng thái báo cáo & gửi email phản hồi!";
        }
    }

}
