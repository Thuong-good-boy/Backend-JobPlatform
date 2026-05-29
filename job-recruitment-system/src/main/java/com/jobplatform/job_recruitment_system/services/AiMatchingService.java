package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.dtos.Response.MatchResultReponse;
import com.jobplatform.job_recruitment_system.models.*;
import com.jobplatform.job_recruitment_system.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiMatchingService {
     private final AiOcrService aiOcrService;
     private final MatchScoreRepository matchScoreRepository;
     private final JobRepository jobRepository;
     private final CvRepository cvRepository;
    private  final ObjectMapper objectMapper;
    @Async
    public void processNewCv(Cv cv) {
        try {
            JsonNode cvDataNode = objectMapper.readTree(cv.getCvData());
            JsonNode  skillsArray= cvDataNode.get("skills");
            if(skillsArray == null || !skillsArray.isArray() || skillsArray.isEmpty()){
                log.warn("CV này không có skill nào để đối chiếu!");
                return;
            }
            List<String> skillList = new ArrayList<>();
            for(JsonNode skillNode : skillsArray){
                skillList.add("\""+skillNode.asText()+"\"");
            }
            String searchQuery = String.join("OR",skillList);
            log.info(" Từ khóa đẩy xuống Postgres: " + searchQuery);
            List<Job> top10Jobs = jobRepository.findTop10MatchingJob(searchQuery);
            for (Job job : top10Jobs) {
                try {
                    calculateAndSave(cv, job);
                    Thread.sleep(5000);
                } catch (Exception e) {
                    log.error(" Lỗi AI Matching tại Job {}: {}", job.getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Async
    public void calculateAndSave(Cv cv, Job job) {
        try {
            MatchResultReponse result = aiOcrService.calculateMatchScore(cv.getCvData(), job);

            if (result == null) return;

            MatchScore ms = new MatchScore();
            ms.setJob(job);
            ms.setCv(cv);

            if (ms.getId() == null) {
                ms.setId(new MatchScoreId(job.getId(), cv.getId()));
            }

            ms.setScore(result.getScore());

            String safeReason = result.getReason().replace("\"", "'");
            ms.setMatch_details("{\"reason\": \"" + safeReason + "\"}");

            matchScoreRepository.save(ms);
            System.out.println(">>> Đã lưu điểm cho Job: " + job.getTitle());

        } catch (Exception e) {
            System.err.println("Lỗi tính điểm cho Job " + job.getId() + ": " + e.getMessage());
        }
    }

    @Async

    public void processNewJob(Job job) {
        try {
            List<String> jobSkills = job.getSkills().stream().map(skill -> skill.getSkillName()).toList();

            if (jobSkills == null || jobSkills.isEmpty()) {
                log.warn("Job này không có yêu cầu skill nào để đối chiếu!");
                return;
            }

            List<String> formattedSkills = new ArrayList<>();
            for (String skill : jobSkills) {
                formattedSkills.add("\"" + skill + "\"");
            }
            String searchQuery = String.join("OR", formattedSkills);
            log.info("Từ khóa Job đẩy xuống Postgres để tìm CV: " + searchQuery);

            List<Cv> top10Cvs = cvRepository.findTop10MatchingCvs(searchQuery);

            for (Cv cv : top10Cvs) {
                try {
                    calculateAndSave(cv, job);

                    Thread.sleep(5000);
                } catch (Exception e) {
                    log.error("Lỗi AI Matching tại CV {}: {}", cv.getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Lỗi nghiêm trọng trong luồng xử lý Job bất đồng bộ: {}", e.getMessage());
        }
    }

    @Async
    public void syncLegacyData() {
        System.out.println(" [ADMIN] BẮT ĐẦU ĐỒNG BỘ DỮ LIỆU CŨ...");

        List<Cv> allCvs = cvRepository.findAll();
        List<Job> allJobs = jobRepository.findAll();

        int count = 0;
        int skipped = 0;

        for (Cv cv : allCvs) {
            for (Job job : allJobs) {
                MatchScoreId id = new MatchScoreId(job.getId(), cv.getId());
                if (!matchScoreRepository.existsById(id)) {
                    calculateAndSave(cv, job);
                    count++;
                } else {
                    skipped++;
                }
            }
        }

        System.out.println(" [ADMIN] ĐÃ ĐỒNG BỘ XONG!");
        System.out.println(" Thống kê: Tính mới " + count + " lượt | Bỏ qua " + skipped + " lượt.");
    }
}