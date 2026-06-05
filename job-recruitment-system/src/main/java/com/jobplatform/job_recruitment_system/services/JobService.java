package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.dtos.Response.CompanyJobsByCandidateResponse;
import com.jobplatform.job_recruitment_system.dtos.Response.ListJobResponse;
import com.jobplatform.job_recruitment_system.dtos.request.JobPostRequest;
import com.jobplatform.job_recruitment_system.dtos.Response.JobRecommendationResponse;
import com.jobplatform.job_recruitment_system.dtos.Response.JobResponse;
import com.jobplatform.job_recruitment_system.enums.JobStatus;
import com.jobplatform.job_recruitment_system.enums.NotificationType;
import com.jobplatform.job_recruitment_system.enums.Role;
import com.jobplatform.job_recruitment_system.exceptions.AppException;
import com.jobplatform.job_recruitment_system.exceptions.ErrorCode;
import com.jobplatform.job_recruitment_system.mapper.JobMapper;
import com.jobplatform.job_recruitment_system.models.*;
import com.jobplatform.job_recruitment_system.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.history.Revisions;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.StringNode;


import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobService {

     private final JobRepository    jobRepository;
     private final UserService userService;
     private final CompanyRepository companyRepository;
     private final SkillRepository skillRepository;
     private final CvService cvService;
     private final MatchScoreRepository matchScoreRepository;
    private final ApplicationRepository applicationRepository;
    private final SavedJobRepository savedJobRepository;
    private final JobMapper jobMapper;
    private  final  UserSubscriptionRepository userSubscriptionRepository;
    private  final  AiMatchingService aiMatchingService;
    private  final  NotificationRepository notificationRepository;
    private  final MessageSource messageSource;
    private  final SimpMessagingTemplate messagingTemplate;
    private  final ObjectMapper objectMapper;
    public Slice<Job> getJobsForUser(int page, int size) {
        Pageable pageable = PageRequest.of(page,size);
            return jobRepository.findAllJobsOpen(pageable);
    }
    public List<Job>  getJobsPro(){
       try {
           Long uerId = userService.getCurrentUserId();
           if(uerId== null){
               List<Job> topJobs = jobRepository.findTop3ProJobsRoundRobin();
               incrementViewCountAsync(topJobs);
               setLastBoostedAt(topJobs);
               return  topJobs;
           }
           Cv cv = cvService.getFirstCv(uerId).orElse(null);
           if(cv == null){
               List<Job> topJobs = jobRepository.findTop3ProJobsRoundRobin();
               incrementViewCountAsync(topJobs);
               setLastBoostedAt(topJobs);
               return  topJobs;

           }
           System.out.println(cv.getCvName());
           String cvData = cv.getCvData();
           JsonNode rootNode = objectMapper.readTree(cvData);
           JsonNode skillsNode = rootNode.path("skills");
           List<String> formattedSkills = new ArrayList<>();
           if (skillsNode.isArray()) {
               for (JsonNode skillNode : skillsNode) {
                   String skill = skillNode.asText().trim();
                   if (!skill.isEmpty()) {
                       if (skill.contains(" ")) {
                           skill = "(" + skill.replace(" ", " & ") + ")";
                       }
                       formattedSkills.add(skill);
                   }
               }
           }
           String skillQueryString = String.join(" | ", formattedSkills);
           List<Job> topJobs = jobRepository.findTop3ProJobsBySkills(skillQueryString);
           setLastBoostedAt(topJobs);
           incrementViewCountAsync(topJobs);
           return topJobs;

       }catch (Exception e){
           e.printStackTrace();
           return new ArrayList<>();
       }

    }
    public Page<Job> getJobsForAdmin(int page, int size, String search, String status) {
        Pageable pageable = PageRequest.of(page,size);
            if(status.equals("ALL")){
           return   jobRepository.findAllForAdmin(pageable);
        }else{
            JobStatus jobStatus = JobStatus.valueOf(status);
            return jobRepository.findAllByStatusForAdmin(pageable, search, jobStatus);
        }

    }
    @Async
    @Transactional
    public void incrementViewCountAsync(List<Job> jobList) {
        if (jobList == null || jobList.isEmpty()) {
            return;
        }
        List<Long> jobIds = jobList.stream().map(Job::getId).toList();
        jobRepository.incrementViewCountForJobs(jobIds);
    }
    @Transactional
    @Async
    public void setLastBoostedAt(List<Job> jobList) {
        if (jobList == null || jobList.isEmpty()) {
            return;
        }
        for (Job job : jobList) {
            job.setLastBoostedAt(LocalDateTime.now());
        }
        jobRepository.saveAll(jobList);
    }

    public Page<JobRecommendationResponse> getJobRecommendationResponses(int page, int size){

        Pageable pageable = PageRequest.of(page,size);
        Long userId = userService.getCurrentUserId();
        Cv cv = cvService.getFirstCv(userId).orElseThrow(()->new AppException(ErrorCode.CV_004));
       Page<JobRecommendationResponse>  jobRecommendationResponses= matchScoreRepository.findRecommendedJobsByCvId(cv.getId(),pageable);
        return  jobRecommendationResponses;
    }


    public JobResponse mapToResponse(Job job, int score, List<String> matchSkills) {
        return JobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .companyName(job.getCompany().getCompanyName())
                .logoUrl(job.getCompany().getLogoUrl())
                .salaryRange(String.format("$%,d - $%,d", job.getSalaryMin(), job.getSalaryMax()))
                .skills(job.getSkills().stream().map(Skill::getSkillName).toList())
                .matchScore(score)
                .matchReason(matchSkills.isEmpty()
                        ? "Dựa trên yêu cầu công việc"
                        : "Phù hợp vì bạn có kỹ năng: " + String.join(", ", matchSkills))
                .build();
    }

    public List<JobResponse> getAllJobsResponse() {
        List<Job> jobs = jobRepository.findAll();
        return jobs.stream()
                .map(job -> mapToResponse(job, 0, List.of())) // matchScore tạm để 0
                .toList();
    }

    public Optional<Job> getJobsById(Long jobId){
        return  jobRepository.findById(jobId);
    }


    @Transactional
    public void postJob(JobPostRequest request) {
        Long userId = userService.getCurrentUserId();
        User user = userService.getUserId(userId).orElseThrow(() -> new AppException(ErrorCode.AUTH_008));
        Long companyId= companyRepository.getCompanyId(userId);
        boolean isPro = userSubscriptionRepository.userispro(userId);
        System.out.println("isPro: " + isPro);
        if(!isPro){
            boolean follow = jobRepository.followNotPro(companyId);
            System.out.println("follow: " + follow);
            if (!follow ){
                throw  new AppException(ErrorCode.NOTPRO_02);
            }
        }

        Company companyProfile = companyRepository.findByUser(user)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_003));
        if (!companyProfile.isVerified()) {
            throw new AppException(ErrorCode.COM_005);
        }

        Job job = jobMapper.fromJobPostRequest(request);
        job.setCompany(companyProfile);
        job.setStatus(JobStatus.PENDING);

        if (request.getSkillNames() != null && !request.getSkillNames().isEmpty()) {
            Set<Skill> jobSkills = new HashSet<>();
            for (String skillName : request.getSkillNames()) {
                Skill skill = skillRepository.findBySkillName(skillName)
                        .orElseThrow(() ->  new AppException(ErrorCode.USER_011));
                jobSkills.add(skill);
            }
            job.setSkills(jobSkills);
        }
        Job jobnew= jobRepository.save(job);
        NotificationType type = NotificationType.NEW_JOB_PENDING;
        Locale locale = LocaleContextHolder.getLocale();
        String titleKey ="noti.title."+type.name();
        String messageKey="noti.message."+type.name();

        String title = messageSource.getMessage(titleKey,null,locale);
        String message= messageSource.getMessage(messageKey, new Object[]{companyProfile.getCompanyName(), jobnew.getTitle()},locale);
        Map<String, Object> metadata= new HashMap<>();
        metadata.put("jobId", job.getId());
        metadata.put("companyId", companyProfile.getId());
        metadata.put("targetUrl", "/admin/management");
        Notification notification = new Notification();
        notification.setRecipientId(1L);
        notification.setSenderId(companyProfile.getUser().getId());
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setMetadata(metadata);
        Notification saveNotification1= notificationRepository.save(notification);
        messagingTemplate.convertAndSend(
                "/topic/notifications/"+saveNotification1.getRecipientId(),
                saveNotification1
        );


    }

    public Slice<Job> searchJobs(String keyword,int page, int size) {
        Pageable pageable = PageRequest.of(page,size);
        String cleanKey =  (keyword!=null) ? keyword.trim():"";
        return jobRepository.searchJobs(cleanKey,pageable);
    }
    public  Job getReferenceById(Long jobid){
        return jobRepository.getReferenceById(jobid);
    }

    public List<ListJobResponse> getJobsByCompanyUserId() {
        Long userId = userService.getCurrentUserId();
        Long companyId= companyRepository.getCompanyId(userId);
        List<Job> jobList = jobRepository.findByCompanyIdCustom(companyId);

        return jobList.stream().map(job -> {
            ListJobResponse response = jobMapper.fromJobtoListJobResponse(job);

            Set<String> skillNames = job.getSkills().stream()
                    .map(skill -> skill.getSkillName())
                    .collect(Collectors.toSet());
            response.setSkills(skillNames);

            int totalApps = applicationRepository.gettotalApplications(job.getId()).intValue();
            int newApps = applicationRepository.getnewApplications(job.getId()).intValue();

            response.setTotalApplications(totalApps);
            response.setNewApplications(newApps);

            return response;

        }).collect(Collectors.toList());
    }
    @Transactional
    public Job updateJobStatus(Long jobId, JobStatus newStatus) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_001));
        User user = userService.getUserId(userService.getCurrentUserId()).orElseThrow(()-> new AppException(ErrorCode.AUTH_008));
        if (!Role.ADMIN.equals(user.getRole())) {
            throw new AppException(ErrorCode.JOB_005);
        }
         job.setStatus(newStatus);
        Job jobnew =  jobRepository.save(job);
        if(newStatus.equals(JobStatus.OPEN)){
            aiMatchingService.processNewJob(jobnew);
        }
        Company companyProfile = jobnew.getCompany();

        NotificationType type;
        if(JobStatus.OPEN.equals(newStatus)){
           type = NotificationType.JOB_CHANGE_STATUS;
        }else{
            type =NotificationType.JOB_REJECTED;
        }

        Locale locale = LocaleContextHolder.getLocale();
        String titleKey ="noti.title."+type.name();
        String messageKey="noti.message."+type.name();

        String title = messageSource.getMessage(titleKey,null,locale);
        String message= messageSource.getMessage(messageKey, new Object[]{companyProfile.getCompanyName(), jobnew.getTitle()},locale);
        Map<String, Object> metadata= new HashMap<>();
        metadata.put("jobId", job.getId());
        metadata.put("companyId", companyProfile.getId());
        metadata.put("targetUrl", "/company/jobs");
        Notification notification = new Notification();
        notification.setRecipientId(companyProfile.getUser().getId());
        notification.setSenderId(1L);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setMetadata(metadata);
        Notification saveNotification1= notificationRepository.save(notification);
        messagingTemplate.convertAndSend(
                "/topic/notifications/"+saveNotification1.getRecipientId(),
                saveNotification1
        );
        return jobnew;

    }

    @Transactional
    public Job updateJob(Long jobId, JobPostRequest request, Long userId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_001));
        User user = userService.getUserId(userService.getCurrentUserId()).orElseThrow(()->new AppException(ErrorCode.AUTH_008));
        if (!job.getCompany().getUser().getId().equals(userId) && !Role.ADMIN.equals(user.getRole())) {
            throw new AppException(ErrorCode.JOB_004);
        }
        jobMapper.updateJob(request, job);

        if (request.getSkillNames() != null) {
            Set<Skill> jobSkills = new HashSet<>();
            for (String skillName : request.getSkillNames()) {
                Skill skill = skillRepository.findBySkillName(skillName)
                        .orElseGet(() -> skillRepository.save(new Skill(skillName)));
                jobSkills.add(skill);
            }
            job.setSkills(jobSkills);
        }
        return jobRepository.save(job);
    }

    @Transactional
    public void deleteJob(Long jobId, Long userId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_001));

        if (!job.getCompany().getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.JOB_006);
        }

        applicationRepository.deleteByJobId(jobId);

        savedJobRepository.deleteByJobId(jobId);

        job.getSkills().clear();
        jobRepository.save(job);

        jobRepository.delete(job);
    }
    public Page<Job> getalljobforcompany( Long companyId, int page, int size){
        Pageable pageable = PageRequest.of(page,size);
        return jobRepository.findByJCompanyId(pageable,companyId);

    }
    @Transactional
    public  Integer  postJob(Long jobId){
            Long userId= userService.getCurrentUserId();
            User user = userService.getUserId(userId).orElse(null);
            if(user==null){
                throw  new AppException(ErrorCode.AUTH_008);
            }
            Company company = companyRepository.findByUser(user).orElse(null);
            if(company==null){
                throw  new AppException(ErrorCode.COM_001);
            }
            int remainingBoosts = company.getRemainingBoosts();
            if(remainingBoosts<= 0){
                throw  new AppException(ErrorCode.NOTPRO_03);
            }
            company.setRemainingBoosts(remainingBoosts - 1);
            companyRepository.save(company);
            Job job = jobRepository.findById(jobId).orElseThrow(()-> new AppException(ErrorCode.JOB_001));
            LocalDateTime now= LocalDateTime.now();
            LocalDateTime  trendingUntil= job.getTrendingUntil();
            if(Boolean.TRUE.equals(job.getIsTrending()) && trendingUntil!= null && trendingUntil.isAfter(now)){
                job.setTrendingUntil(trendingUntil.plusHours(24));
            }else {
                job.setIsTrending(true);
                job.setTrendingUntil(now.plusHours(24));
            }
            jobRepository.save(job);
            return company.getRemainingBoosts();
    }
    public Revisions<Integer, Job> getJobHistory(@PathVariable Long id){
        return jobRepository.findRevisions(id);
    }
    @Async
    public void incrementClickCount(Long jobId) {
        Job job = jobRepository.findById(jobId).orElseThrow(()-> new AppException(ErrorCode.JOB_001));
        job.setClickCount(job.getClickCount()+1);
        jobRepository.save(job);
    }
    @Async
    public  void lockListJobs(List<Job> jobList){
        for(Job j : jobList){
            j.setStatus(JobStatus.CLOSED);
            jobRepository.save(j);
        }
    }
}