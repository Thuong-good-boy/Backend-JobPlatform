package com.jobplatform.job_recruitment_system.services;


import com.jobplatform.job_recruitment_system.dtos.Response.AppliedJobResponse;
import com.jobplatform.job_recruitment_system.dtos.request.ApplyRequest;
import com.jobplatform.job_recruitment_system.dtos.Response.ApplicationOnlyJobResponse;
import com.jobplatform.job_recruitment_system.dtos.request.ApplicationRequest;
import com.jobplatform.job_recruitment_system.enums.AppStatus;
import com.jobplatform.job_recruitment_system.enums.NotificationType;
import com.jobplatform.job_recruitment_system.exceptions.AppException;
import com.jobplatform.job_recruitment_system.exceptions.ErrorCode;
import com.jobplatform.job_recruitment_system.mapper.ApplicationMapper;
import com.jobplatform.job_recruitment_system.models.*;
import com.jobplatform.job_recruitment_system.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ApplicationService {
    
    private final ApplicationRepository applicationRepository;
     private final CvRepository cvRepository;
     private final JobRepository jobRepository;
     private final UserRepository userRepository;
    private  final  NotificationService notificationService;
    private  final  UserService userService;
    private  final  CompanyRepository companyRepository;
    private  final ApplicationMapper applicationMapper;
    @Transactional
    public Application applyForJob(ApplicationRequest request,Long jobId) {
        Long  userId= userService.getCurrentUserId();
        Job job = jobRepository.getReferenceById(jobId);
        Cv cv = cvRepository.findById(request.getCvId())
                .orElseThrow(() -> new AppException(ErrorCode.CV_002));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.AUTH_008));
        boolean ispro = userService.getCurrentUserIsPro();
        if(!ispro&& applicationRepository.getCountApply(userId)>=1){
           throw  new AppException(ErrorCode.NOTPRO_01);
        }
        Application app =  applicationMapper.formApplicationRequesttoApplication(request);
        app.setUser(user);
        app.setCv(cv);
        app.setJob(job);
        return applicationRepository.save(app);
    }
    @Transactional
    public void applyJob(ApplyRequest request){
        Long userId = userService.getCurrentUserId();
        User user= userService.getUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_012));
        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new AppException(ErrorCode.JOB_001));
        Cv cv = cvRepository.findById(request.getCvId())
                .orElseThrow(() -> new AppException(ErrorCode.CV_002));

        if (!cv.getUser().getId().equals(user.getId())) {
             new AppException(ErrorCode.CV_005);
        }
        if (applicationRepository.existsByJobIdAndCvId(request.getJobId(), request.getCvId())) {
            new AppException(ErrorCode.JOB_007);
        }
        try {
            Application app = new Application();
            app.setJob(job);
            app.setCv(cv);
            app.setCoverLetter(request.getCoverLetter());
            Application application= applicationRepository.save(app);
            NotificationType type = NotificationType.NEW_APPLICATION;
            Map<String,Object> metadata = new HashMap<>();
            metadata.put("jobId",request.getJobId());
            metadata.put("targetUrl","/company/applications" + application.getId());
            notificationService.sendNotification(
                    job.getCompany().getUserId(),
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

            // Xử lý Lương (Gộp Min và Max)
            if (job.getSalaryMin() != null && job.getSalaryMax() != null) {
                dto.setSalary(job.getSalaryMin() + " - " + job.getSalaryMax());
            } else {
                dto.setSalary("Thỏa thuận");
            }

            // Xử lý Công ty (Dùng companyName)
            if (company != null) {
                dto.setCompanyName(company.getCompanyName());
                dto.setLogoUrl(company.getLogoUrl() != null ? company.getLogoUrl() : "https://ui-avatars.com/api/?name=" + company.getCompanyName());
            } else {
                dto.setCompanyName("Công ty bảo mật");
                dto.setLogoUrl("https://ui-avatars.com/api/?name=C");
            }

            // Xử lý Ngày ứng tuyển (Dùng appliedAt có sẵn trong Model của ông)
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
        if(applications == null){
            return  List.of();
        }
        if("score".equalsIgnoreCase(sortBy)){
            applications.sort((a,b)-> Double.compare(
                    a.getAiMatchScore() !=null ? a.getAiMatchScore() :0.0,
                    b.getAiMatchScore() !=null ? b.getAiMatchScore(): 0.0
            ));
        }else{
            java.util.Collections.reverse(applications);
        }
        return applications;

    }
    @Transactional
    public void updateStatus(Long applyId, AppStatus newStatus) {
        Application app = applicationRepository.findById(applyId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn ứng tuyển!"));

        app.setStatus(newStatus);
        applicationRepository.save(app);
    }
    @Transactional
    public void updateApplicationStatus(Long applicationId, AppStatus newStatus) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_012));

        application.setStatus(newStatus);
        applicationRepository.save(application);

//        if (newStatus == AppStatus.INTERVIEW) {
//            Company company = application.getJob().getCompany();
//            User candidate = application.getCv().getUser();
//            Job job = application.getJob();
//
//            boolean roomExists = chatRoomRepository.existsByCompanyIdAndCandidateIdAndJobId(company.getUserId(), candidate.getId(), job.getId());
//
//            if (!roomExists) {
//                ChatRoom chatRoom = new ChatRoom();
//                chatRoom.setCompany(company);
//                chatRoom.setCandidate(candidate);
//                chatRoom.setJob(job);
//                chatRoom.setCreatedAt(LocalDateTime.now());
//
//                chatRoomRepository.save(chatRoom);
//                String notificationMessage = "Chúc mừng! Công ty " + company.getCompanyName() +
//                        " đã duyệt hồ sơ của bạn cho vị trí " + job.getTitle() + ". Hãy vào mục Tin nhắn nhé!";
//                Notification notif = new Notification();
//                notif.setUser(candidate);
//                notif.setMessage(notificationMessage);
//                notif.setRead(false);
//                notificationRepository.save(notif);
//                // gửi qua kênh, xong bên front nhận
//                String destination = "/topic/notifications/" + candidate.getEmail();
//                messagingTemplate.convertAndSend(destination, notificationMessage);
//            }
//        }
    }
}
