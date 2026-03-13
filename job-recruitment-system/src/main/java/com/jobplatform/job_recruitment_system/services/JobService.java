package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.dtos.JobPostRequest;
import com.jobplatform.job_recruitment_system.dtos.JobRecommendationDTO;
import com.jobplatform.job_recruitment_system.dtos.JobResponse;
import com.jobplatform.job_recruitment_system.models.*;
import com.jobplatform.job_recruitment_system.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JobService {

    @Autowired private JobRepository jobRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CompanyRepository companyRepository;
    @Autowired private SkillRepository skillRepository;
    @Autowired private  CvRepository cvRepository;
    @Autowired private MatchScoreRepository matchScoreRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Object getJobsForUser(Long userId) {
        if (userId == null) {
            return jobRepository.findAll();
        }

        Cv userCv = cvRepository.findFirstByUser_IdOrderByCreatedAtDesc(userId).orElse(null);
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




    @Transactional
    public Job postJob(JobPostRequest request) {
        // 1. Lấy email của User đang đăng nhập từ Security Context
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Tìm User trong DB
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // 3. Tìm hồ sơ Công ty của User này (Chỉ Employer mới có Company)
        Company companyProfile = companyRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Bạn phải tạo hồ sơ công ty trước khi đăng tin!"));

        // 4. Tạo đối tượng Job mới và map dữ liệu từ request
        Job job = new Job();
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setSalaryMin(request.getSalaryMin());
        job.setSalaryMax(request.getSalaryMax());
        job.setLocation(request.getLocation());
        job.setCreatedAt(LocalDateTime.now());

        // Thiết lập mối quan hệ với Company
        job.setCompany(companyProfile);

        // 5. Xử lý danh sách Skill (nếu có)
        if (request.getSkillNames() != null) {
            Set<Skill> jobSkills = new HashSet<>();
            for (String skillName : request.getSkillNames()) {
                // Tìm skill theo tên, nếu chưa có thì tạo mới (Chuẩn hóa dữ liệu AI)
                Skill skill = skillRepository.findBySkillName(skillName)
                        .orElseGet(() -> skillRepository.save(new Skill(skillName)));
                jobSkills.add(skill);
            }
            job.setSkills(jobSkills);
        }

        // 6. Lưu vào Database và trả về kết quả
        return jobRepository.save(job);
    }

    public List<JobRecommendationDTO> searchJobs(String keyword, Long userId) {
        Cv userCv = (userId == null) ? null : cvRepository.findFirstByUser_IdOrderByCreatedAtDesc(userId).orElse(null);

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

        Cv userCv = cvRepository.findFirstByUser_IdOrderByCreatedAtDesc(userId).orElse(null);
        if (userCv == null) {
            return jobRepository.findById(jodId);
        }
        return  matchScoreRepository.findRecommendedJobsByJobId(jodId,userCv.getId());
    }

}