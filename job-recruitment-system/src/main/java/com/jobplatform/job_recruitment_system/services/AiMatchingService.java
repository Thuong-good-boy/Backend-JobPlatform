package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.dtos.MatchResult;
import com.jobplatform.job_recruitment_system.models.*;
import com.jobplatform.job_recruitment_system.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AiMatchingService {
    @Autowired private AiOcrService aiOcrService;
    @Autowired private MatchScoreRepository matchScoreRepository;
    @Autowired private JobRepository jobRepository;
    @Autowired private CvRepository cvRepository;

    @Async
    public void processNewCv(Cv cv) {
        List<Job> allJobs = jobRepository.findAll();
        for (Job job : allJobs) {
            calculateAndSave(cv, job);
        }
    }

    @Async
    public void processNewJob(Job job) {
        System.out.println(" Đang tính điểm cho Job mới: " + job.getTitle());
        List<Cv> allCvs = cvRepository.findAll();
        for (Cv cv : allCvs) {
            calculateAndSave(cv, job); // Tận dụng lại hàm cũ
        }
    }
    // Hàm dùng chung để tính và lưu
    public void calculateAndSave(Cv cv, Job job) {
        try {
            // Gọi AI tính điểm
            MatchResult result = aiOcrService.calculateMatchScore(cv.getCvData(), job.getDescription());

            // Map dữ liệu vào Entity để khớp 5 cột DB
            MatchScore ms = new MatchScore();
            ms.setJob(job);
            ms.setCv(cv);
            ms.getId().setJobId(job.getId());
            ms.getId().setCvId(cv.getId());
            ms.setScore(result.getScore());
            // Đóng gói lý do vào JSON cho cột match_details
            ms.setMatch_details("{\"reason\": \"" + result.getReason() + "\"}");

            matchScoreRepository.save(ms);
            Thread.sleep(10000);
        } catch (Exception e) {
            System.err.println("Lỗi tính điểm cho CV " + cv.getId() + " - Job " + job.getId());
            try {
                Thread.sleep(15000); // dừng 15 giây
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
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
                // TẠO KHÓA CHÍNH ĐỂ KIỂM TRA
                MatchScoreId id = new MatchScoreId(job.getId(), cv.getId());

                // NẾU CHƯ  A CÓ ĐIỂM THÌ MỚI GỌI AI
                if (!matchScoreRepository.existsById(id)) {
                    calculateAndSave(cv, job); // Hàm bạn đã viết ở bước trước
                    count++;
                } else {
                    skipped++; // Có rồi thì bỏ qua
                }
            }
        }

        System.out.println(" [ADMIN] ĐÃ ĐỒNG BỘ XONG!");
        System.out.println(" Thống kê: Tính mới " + count + " lượt | Bỏ qua " + skipped + " lượt.");
    }
}