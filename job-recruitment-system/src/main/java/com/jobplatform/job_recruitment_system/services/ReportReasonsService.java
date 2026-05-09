package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.dtos.request.ReportReasonRequest;
import com.jobplatform.job_recruitment_system.enums.ReportStatus;
import com.jobplatform.job_recruitment_system.exceptions.AppException;
import com.jobplatform.job_recruitment_system.exceptions.ErrorCode;
import com.jobplatform.job_recruitment_system.mapper.ReportReasonMapper;
import com.jobplatform.job_recruitment_system.models.ReportReasons;
import com.jobplatform.job_recruitment_system.repositories.ReportReasonsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportReasonsService {
    private  final ReportReasonsRepository reportReasonsRepository;
    private  final ReportReasonMapper mapper;
    public Page<ReportReasons> getReportReasons(int page, int size){
        Pageable pageable = PageRequest.of(page,size);
        return reportReasonsRepository.findAll(pageable);
    }
    public  void createRepost(ReportReasonRequest request){
        ReportReasons reasons = mapper.fromRequest(request);
        reportReasonsRepository.save(reasons);
    }
    public  void updateReport(Long reportId,ReportReasonRequest request){
        ReportReasons reasons = reportReasonsRepository.findById(reportId).orElseThrow(()-> new AppException(ErrorCode.REPORTREASON_01));
        mapper.update(reasons,request);
        reportReasonsRepository.save(reasons);
    }
    public  void updateStatus(Long reportId){
        ReportReasons reasons = reportReasonsRepository.findById(reportId).orElseThrow(()-> new AppException(ErrorCode.REPORTREASON_01));
        reasons.setActive(!reasons.isActive());
        reportReasonsRepository.save(reasons);
    }
    public List<ReportReasons> getReportCompany(){
        return  reportReasonsRepository.findForCompany();
    }
    public List<ReportReasons> getReportCandidate(){

        return  reportReasonsRepository.findForCandidate();
    }
    public List<ReportReasons> getReportJob(){
        return  reportReasonsRepository.findForJob();
    }


}
