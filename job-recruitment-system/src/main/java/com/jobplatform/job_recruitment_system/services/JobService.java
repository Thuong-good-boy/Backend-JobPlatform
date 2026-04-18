package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.dtos.request.JobPostRequest;
import com.jobplatform.job_recruitment_system.dtos.JobRecommendationDTO;
import com.jobplatform.job_recruitment_system.dtos.JobResponse;
import com.jobplatform.job_recruitment_system.exceptions.AppException;
import com.jobplatform.job_recruitment_system.exceptions.ErrorCode;
import com.jobplatform.job_recruitment_system.mapper.JobMapper;
import com.jobplatform.job_recruitment_system.models.*;
import com.jobplatform.job_recruitment_system.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobService {

     private final JobRepository jobRepository;
     private final UserService userService;
     private final CompanyRepository companyRepository;
     private final SkillRepository skillRepository;
     private final CvService cvService;
     private final MatchScoreRepository matchScoreRepository;
    private final ApplicationRepository applicationRepository;
    private final SavedJobRepository savedJobRepository;
    private final JobMapper jobMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Object getJobsForUser(Long userId) {
        if (userId == null) {
            return jobRepository.findAll();
        }

        Cv userCv = cvService.getFirstCv (userId).orElse(null);
        if (userCv == null) {
            return jobRepository.findAll();
        }

        return matchScoreRepository.findRecommendedJobsByCvId(userCv.getId());
    }


    public JobResponse mapToResponse(Job job, int score, List<String> matchSkills) {
        return JobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .companyName(job.getCompany().getCompanyName())
                .logoUrl(job.getCompany().getLogoUrl()) // Giả sử bảng Company có field logo
                // Lưu ý: Tên field trong postJob là salaryMin/Max, hãy dùng đúng getter đó
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
    public Job postJob(JobPostRequest request) {
        Long userId = userService.getCurrentUserId();
        User user = userService.getUserId(userId).orElseThrow(() -> new AppException(ErrorCode.AUTH_008));

        Company companyProfile = companyRepository.findByUser(user)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_003));

        if (!companyProfile.isVerified()) {
            throw new AppException(ErrorCode.COM_005);
        }

        Job job = jobMapper.fromJobPostRequest(request);
        job.setCompany(companyProfile);
        job.setStatus(JobStatus.OPEN);

        if (request.getSkillNames() != null && !request.getSkillNames().isEmpty()) {
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

    public List<JobRecommendationDTO> searchJobs(String keyword, Long userId) {
        Cv userCv = (userId == null) ? null :  cvService.getFirstCv (userId).orElse(null);

        if (userCv == null) {
            List<Job> jobs = jobRepository.searchJobs(keyword);
            return jobs.stream()
                    .map(job -> new JobRecommendationDTO(job, 0.0, ""))
                    .collect(Collectors.toList());
        }

        List<MatchScore> scores = matchScoreRepository.findRecommendedEntitiesByKeyword(userCv.getId(), keyword);

        return scores.stream().map(ms -> {
            String reason = "";
            try {
                // Chú ý: ms.getMatch_details() phải khớp với tên trong Entity của bạn
                if (ms.getMatch_details() != null) {
                    JsonNode node = objectMapper.readTree(ms.getMatch_details());
                    reason = node.has("reason") ? node.get("reason").asText() : "";
                }
            } catch (Exception e) {
                reason = "Phù hợp với mục tiêu nghề nghiệp của bạn";
            }

            return new JobRecommendationDTO(
                    ms.getJob(),
                    ms.getScore(),
                    reason
            );
        }).collect(Collectors.toList());
    }
    public  Job getReferenceById(Long jobid){
        return jobRepository.getReferenceById(jobid);
    }
    public  Object getJobByjobIdandCvId(Long jodId,Long userId ){
        if (userId == null) {
            return jobRepository.findById(jodId);
        }

        Cv userCv =  cvService.getFirstCv (userId).orElse(null);
        if (userCv == null) {
            return jobRepository.findById(jodId);
        }
        return  matchScoreRepository.findRecommendedJobsByJobId(jodId,userCv.getId());
    }
    public List<Job> getJobsByCompanyUserId(Long userId) {
        return  jobRepository.findByCompanyIdCustom(userId);
    }
    @Transactional
    public Job updateJobStatus(Long jobId, JobStatus newStatus, Long userId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_001));

        if (!job.getCompany().getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.JOB_005);
        }
        job.setStatus(newStatus);
        return jobRepository.save(job);
    }

    @Transactional
    public Job updateJob(Long jobId, JobPostRequest request, Long userId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_001));

        if (!job.getCompany().getUser().getId().equals(userId)) {
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

    public List<JobRecommendationDTO> findRecommendedJobsByCvIdAndCompanyId(Long userId, Long companyId) {
        Cv cv = cvService.getFirstCv(userId)
                .orElseThrow(() -> new AppException(ErrorCode.CV_004));

        return matchScoreRepository.findRecommendedJobsByCvIdAndCompanyId(cv.getId(), companyId);
    }

}