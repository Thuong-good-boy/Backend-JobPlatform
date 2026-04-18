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
        // Nghỉ 5 giây để "cách ly" với bước đọc CV trước đó
        try { Thread.sleep(5000); } catch (InterruptedException ignored) {}

        List<Job> allJobs = jobRepository.findAll();
        for (Job job : allJobs) {
            calculateAndSave(cv, job);
            try { Thread.sleep(10000); } catch (InterruptedException ignored) {}
        }
    }

    public void calculateAndSave(Cv cv, Job job) {
        try {
            MatchResult result = aiOcrService.calculateMatchScore(cv.getCvData(), job.getDescription());

            if (result == null) return;

            MatchScore ms = new MatchScore();
            ms.setJob(job);
            ms.setCv(cv);

            // Đảm bảo khởi tạo ID nếu là EmbeddedId
            if (ms.getId() == null) {
                ms.setId(new MatchScoreId(job.getId(), cv.getId()));
            }

            ms.setScore(result.getScore());

            // Dùng String.format để tránh lỗi nháy kép trong reason làm hỏng JSON
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
        System.out.println(" Đang tính điểm cho Job mới: " + job.getTitle());
        List<Cv> allCvs = cvRepository.findAll();
        for (Cv cv : allCvs) {
            calculateAndSave(cv, job); // Tận dụng lại hàm cũ
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