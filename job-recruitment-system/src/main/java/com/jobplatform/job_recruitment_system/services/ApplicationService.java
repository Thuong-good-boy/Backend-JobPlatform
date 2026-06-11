package com.jobplatform.job_recruitment_system.services;


import com.jobplatform.job_recruitment_system.dtos.Response.AppliedJobResponse;
import com.jobplatform.job_recruitment_system.dtos.request.ApplyRequest;
import com.jobplatform.job_recruitment_system.dtos.Response.ApplicationOnlyJobResponse;
import com.jobplatform.job_recruitment_system.dtos.request.ApplicationRequest;
import com.jobplatform.job_recruitment_system.dtos.request.ChatMessageRequest;
import com.jobplatform.job_recruitment_system.enums.AppStatus;
import com.jobplatform.job_recruitment_system.enums.NotificationType;
import com.jobplatform.job_recruitment_system.exceptions.AppException;
import com.jobplatform.job_recruitment_system.exceptions.ErrorCode;
import com.jobplatform.job_recruitment_system.mapper.ApplicationMapper;
import com.jobplatform.job_recruitment_system.models.*;
import com.jobplatform.job_recruitment_system.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class  ApplicationService {
    
    private final ApplicationRepository applicationRepository;
     private final CvRepository cvRepository;
     private final JobRepository jobRepository;
     private final UserRepository userRepository;
    private  final  NotificationService notificationService;
    private  final  UserService userService;
    private final  ChatRoomService chatRoomService;
    private  final CandidateRepository candidateRepository;
    private  final MatchScoreRepository matchScoreRepository;
    private  final AiMatchingService aiMatchingService;

    public void applyForJob(Long jobId , Long userId) {
        Job job = jobRepository.getReferenceById(jobId);
        Cv cv = cvRepository.getCvByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.CV_006));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.AUTH_008));
        try {
        Application app = new Application();
        app.setJob(job);
        app.setUser(user);
        app.setCv(cv);
        app.setFullName(user.getFullName());
        app.setStatus(AppStatus.APPLIED);
        applicationRepository.save(app);

        Boolean hasMacthScore = matchScoreRepository.existsByJobIdAndCvId(jobId, cv.getId());
        System.out.println("jobId" + jobId + "    cvId" + cv.getId());
        System.out.println(hasMacthScore);
        if(!hasMacthScore){
            aiMatchingService.calculateAndSave(cv,job);
        }

        NotificationType type = NotificationType.NEW_APPLICATION;
        Map<String,Object> metadata = new HashMap<>();
        metadata.put("jobId",jobId);
        metadata.put("targetUrl","/company/jobs/detail/" + jobId);
        System.out.println("có vào check 01" );
        notificationService.sendNotification(
                job.getCompany().getUser().getId(),
                userId,
                type,
                metadata,
                user.getFullName(),
                job.getTitle()
        );
    }catch (Exception e){
        new AppException(ErrorCode.USER_011);
    }
    }

    public  Boolean checkByJobIdAndCv_User_Id(Long jobId, Long userId){
        return applicationRepository.existsByJobIdAndCv_User_Id(jobId,userId);
    }
    public List<AppliedJobResponse> getAppliedJobsByUser(Long userId) {

        List<Application> applications = applicationRepository.findByCv_User_IdOrderByAppliedAtDesc(userId);

        return applications.stream().map(app -> {
            Job job = app.getJob();
            Company company = job.getCompany();

            AppliedJobResponse dto = new AppliedJobResponse();
            dto.setId(app.getId());
            dto.setJobId(job.getId());
            dto.setJobTitle(job.getTitle());
            dto.setLocation(job.getLocation());
            dto.setStatus(app.getStatus().name());

            if (job.getSalaryMin() != null && job.getSalaryMax() != null) {
                dto.setSalary(job.getSalaryMin() + " - " + job.getSalaryMax());
            } else {
                dto.setSalary("Thỏa thuận");
            }

            if (company != null) {
                dto.setCompanyName(company.getCompanyName());
                dto.setLogoUrl(company.getLogoUrl() != null ? company.getLogoUrl() : "https://ui-avatars.com/api/?name=" + company.getCompanyName());
            } else {
                dto.setCompanyName("Công ty bảo mật");
                dto.setLogoUrl("https://ui-avatars.com/api/?name=C");
            }

            if (app.getAppliedAt() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                dto.setAppliedDate(app.getAppliedAt().format(formatter));
            } else {
                dto.setAppliedDate("Vừa xong");
            }

            return dto;
        }).toList();
    }
    public List<ApplicationOnlyJobResponse> getApplicationsByJobId(Long jobId, String sortBy) {
        List<ApplicationOnlyJobResponse> applications = applicationRepository.findListApplicationIncludeMatch(jobId);

        if (applications == null || applications.isEmpty()) {
            return new ArrayList<>();
        }

        if ("scores".equalsIgnoreCase(sortBy)) {
            applications.sort((a, b) -> Double.compare(
                    b.getAiMatchScore() != null ? b.getAiMatchScore() : 0.0,
                    a.getAiMatchScore() != null ? a.getAiMatchScore() : 0.0
            ));
        } else {
            java.util.Collections.reverse(applications);
        }

        return applications;
    }
    @Transactional
    public Boolean updateApplicationStatus(Long applicationId, AppStatus newStatus) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_012));
        application.setStatus(newStatus);
        Application newApplicationStatus = applicationRepository.save(application);
        Long CandidateId =  candidateRepository.getCandidateIdByUSerId(newApplicationStatus.getUser().getId());
        Candidate  candidate = candidateRepository.getReferenceById(CandidateId);
        Boolean hasChatRoom = chatRoomService.checkHasChatRoom(newApplicationStatus.getJob().getCompany().getId(),CandidateId, application.getJob().getId());
        NotificationType type = (newStatus == AppStatus.INTERVIEW)
                ? NotificationType.INTERVIEW_INVITED
                : NotificationType.APPLICATION_REJECTED;

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("applicationId", applicationId);
        metadata.put("jobId", application.getJob().getId());
        metadata.put("targetUrl", "/jobs/" + application.getJob().getId());

        notificationService.sendNotification(
                candidate.getUser().getId(),
                application.getJob().getCompany().getUser().getId(),
                type,
                metadata,
                application.getJob().getTitle()
        );
        return  hasChatRoom;
    }
    @Transactional
    public  void createRoomChat(Long applicationId){
        Application application = applicationRepository.getReferenceById(applicationId);
        Long candidateId = candidateRepository.getCandidateIdByUSerId(application.getUser().getId());
        chatRoomService.createRoomIfNotExist(
                        application.getJob().getId(),
                        application.getJob().getCompany().getId(),
                         candidateId
                );
    }
}
