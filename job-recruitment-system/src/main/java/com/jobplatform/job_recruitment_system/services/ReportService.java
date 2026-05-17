package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.config.CloudinaryConfig;
import com.jobplatform.job_recruitment_system.dtos.Response.ReportResponse;
import com.jobplatform.job_recruitment_system.dtos.request.ReportProcessRequest;
import com.jobplatform.job_recruitment_system.dtos.request.ReportSubmitRequest;
import com.jobplatform.job_recruitment_system.enums.JobStatus;
import com.jobplatform.job_recruitment_system.enums.NotificationType;
import com.jobplatform.job_recruitment_system.enums.ReportStatus;
import com.jobplatform.job_recruitment_system.enums.TargetType;
import com.jobplatform.job_recruitment_system.exceptions.AppException;
import com.jobplatform.job_recruitment_system.exceptions.ErrorCode;
import com.jobplatform.job_recruitment_system.mapper.ReportMapper;
import com.jobplatform.job_recruitment_system.models.*;
import com.jobplatform.job_recruitment_system.repositories.*;
import lombok.RequiredArgsConstructor;
import org.aspectj.apache.bcel.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private  final  NotificationService notificationService;
    private  final CompanyRepository companyRepository;
    private  final JobRepository jobRepository;

    public void creatReport(ReportSubmitRequest request) throws Exception {
        boolean hasReasonId = request.getReasonId() != null;
        boolean hasCustomReason = request.getCustomReason() != null && !request.getCustomReason().trim().isEmpty();

        if (!hasReasonId && !hasCustomReason) {
            throw new AppException(ErrorCode.REPORTREASON_02);
        }

        Report report = new Report();
        User user = userService.getUserId(userService.getCurrentUserId())
                .orElseThrow(() -> new AppException(ErrorCode.AUTH_008));

        try {
            System.out.println("Bắt đầu xử lý tạo report...");
            report.setReporter(user);
            report.setTargetId(request.getTargetId());
            report.setDescription(request.getDescription());
            report.setStatus(ReportStatus.PENDING);
            if(TargetType.COMPANY.equals(request.getTargettype())){
                report.setTargetType(TargetType.COMPANY);
            }else{
                if(TargetType.CANDIDATE.equals(request.getTargettype())){
                    report.setTargetType(TargetType.CANDIDATE);
                }else{
                    report.setTargetType(TargetType.JOB);
                }

            }
            if (hasReasonId) {
                ReportReasons reasons = reportReasonsRepository.findById(request.getReasonId())
                        .orElseThrow(() -> new AppException(ErrorCode.REPORTREASON_01));
                report.setReportReason(reasons);
            }

            if (hasCustomReason) {
                report.setCustomReason(request.getCustomReason().trim());
            }

            repository.save(report);

            List<ReportEvidence> list = new ArrayList<>();
            if (request.getFiles() != null && !request.getFiles().isEmpty()) {
                for (MultipartFile file : request.getFiles()) {
                    String imageUrl = fileUploadService.uploadFile(file);
                    ReportEvidence reportEvidence = new ReportEvidence();
                    reportEvidence.setImageUrl(imageUrl);
                    reportEvidence.setReport(report);
                    ReportEvidence response = reportEvidenceRepository.save(reportEvidence);
                    list.add(response);
                }
            }

            if (report.getEvidences() == null) {
                report.setEvidences(new ArrayList<>());
            } else {
                report.getEvidences().clear();
            }
            report.getEvidences().addAll(list);

            Report savedReport = repository.save(report);

            NotificationType type = NotificationType.NEW_REPORT;
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("reportId", savedReport.getId());
            metadata.put("targetId", savedReport.getTargetId());
            metadata.put("targetUrl", "/admin/reports");

            String notificationTitle = hasReasonId ? savedReport.getReportReason().getTitle() : savedReport.getCustomReason();

            Long adminId = 1L;
            notificationService.sendNotification(
                    adminId,
                    user.getId(),
                    type,
                    metadata,
                    user.getEmail(),
                    savedReport.getTargetId(),
                    notificationTitle
            );

        } catch (Exception e) {
            System.err.println("LỖI TẠI CREAT-REPORT: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    public Page<ReportResponse> getRepost(int page, int size){
        Pageable pageable = PageRequest.of(page,size);
        Page<Report> reportPage = repository.findAllByCreatedAtDesc(pageable);
        Page<ReportResponse> reportResponses = reportPage.map(
                report -> {
                    ReportResponse dto = mapper.fromentity(report);
                    if(report.getReportReason() != null){
                        dto.setReasonTitle(report.getReportReason().getTitle()  );
                    }else{
                        dto.setReasonTitle(report.getCustomReason() );
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
        System.out.println("note Admin : " + request.getAdminNote());
        Report newReport= repository.save(report);
        try {
            emailService.sendReportFeedbackEmail(
                    newReport.getReporter().getEmail(),
                    newReport.getReporter().getFullName(),
                    newReport.getId(),
                    newReport.getTargetType().toString(),
                    newReport.getReportReason().getTitle(),
                    newReport.getStatus(),
                    newReport.getAdminNote()
            );
        } catch (Exception e) {
            System.err.println("Lỗi gửi mail phản hồi báo cáo: " + e.getMessage());
        }

        if (request.getStatus() == ReportStatus.RESOLVED) {
            String reasonTitle = (newReport.getReportReason() == null)
                    ? newReport.getCustomReason()
                    : newReport.getReportReason().getTitle();
                if (newReport.getTargetType() == TargetType.CANDIDATE){
                    Candidate candidate = candidateRepository.getReferenceById(report.getTargetId());
                    User user  = candidate.getUser();
                    user.setActive(false);
                    userRepository.save(user);
                    try {
                        emailService.sendCandidateAccountLockedEmail(
                                user.getEmail(),
                                user.getFullName(),
                                reasonTitle,
                                newReport.getAdminNote()
                        );
                    }catch (Exception e){
                        System.err.println("Lỗi gửi mail phản hồi báo cáo cho ứng viên : " + e.getMessage());

                    }

                }else{
                    if (newReport.getTargetType() == TargetType.COMPANY){
                        Company company= companyRepository.findById(report.getTargetId()).orElseThrow(()-> new AppException(ErrorCode.COM_001));
                        User user = company.getUser();
                        user.setActive(false);
                        userRepository.save(user);
                        try {
                            emailService.sendCompanyAccountLockedEmail(
                                    user.getEmail(),
                                    company.getCompanyName(),
                                    reasonTitle,
                                    report.getAdminNote()
                            );
                        }catch (Exception e){
                            System.err.println("Lỗi gửi mail phản hồi báo cáo cho company : " + e.getMessage());
                        }
                    }else{
                        Job job = jobRepository.getReferenceById(report.getTargetId());
                        job.setStatus(JobStatus.CLOSED);
                        jobRepository.save(job);
                        Company company = job.getCompany();
                        User user = company.getUser();
                        try {
                            emailService.sendJobRemovedEmail(
                                    user.getEmail(),
                                    company.getCompanyName(),
                                    job.getTitle(),
                                    reasonTitle,
                                    report.getAdminNote()
                            );
                        }catch (Exception e){
                            System.err.println("Lỗi gửi mail phản hồi báo cáo cho job : " + e.getMessage());

                        }
                    }

                }
            return "Đã XÁC NHẬN vi phạm, xử lý tài khoản & gửi email cho người tố cáo!";

        } else if (request.getStatus() == ReportStatus.REJECTED) {
            return "Đã TỪ CHỐI báo cáo & gửi email giải thích cho người tố cáo!";

        } else {

            return "Đã cập nhật trạng thái báo cáo & gửi email phản hồi!";
        }
    }

}
