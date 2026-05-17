package com.jobplatform.job_recruitment_system.services;

import com.cloudinary.Cloudinary;
import com.jobplatform.job_recruitment_system.config.CloudinaryConfig;
import com.jobplatform.job_recruitment_system.exceptions.AppException;
import com.jobplatform.job_recruitment_system.exceptions.ErrorCode;
import com.jobplatform.job_recruitment_system.models.Application;
import com.jobplatform.job_recruitment_system.models.Cv;
import com.jobplatform.job_recruitment_system.models.User;
import com.jobplatform.job_recruitment_system.repositories.ApplicationRepository;
import com.jobplatform.job_recruitment_system.repositories.CvRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CvService {

    private final CvRepository cvRepository;
    private final FileUploadService fileUploadService;
    private final AiOcrService aiOcrService;
    private final AiMatchingService aiMatchingService;
    private  final UserService  userService;
    private  final ApplicationRepository applicationRepository;
    @Transactional
    public Cv uploadAndAnalyze(MultipartFile file) throws Exception {

        Long userId = userService.getCurrentUserId();
        User user = userService.getUserId(userId).orElseThrow(()-> new AppException(ErrorCode.AUTH_008));
        String fileUrl = fileUploadService.uploadFile(file);

        String cvDataJson = aiOcrService.extractCvInfoToJson(file);
        System.out.println();
        Cv cv = new Cv();
        cv.setUser(user);
        cv.setFileUrl(fileUrl);
        cv.setCvData(cvDataJson);
        cv.setCvName(file.getOriginalFilename());
        cv.setActive(true);

        Cv savedCv = cvRepository.save(cv);
        if(userService.getCurrentUserIsPro()) {
            aiMatchingService.processNewCv(savedCv);
        }
        return savedCv;
    }

    public List<Cv> getMyActiveCvs() {
        Long userId = userService.getCurrentUserId();
        return cvRepository.findAllByUser_IdAndActiveTrueOrderByCreatedAtDesc(userId);
    }

    public Optional<Cv> getFirstCv(Long userId){
        return  cvRepository.findFirstByUser_IdAndActiveTrueOrderByCreatedAtDesc(userId);
    }
    public void softDelete(Long cvId) {
        Long userId = userService.getCurrentUserId();
        Cv cv = cvRepository.findById(cvId)
                .orElseThrow(() -> new AppException(ErrorCode.APP_002));

        if (!cv.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.CV_005);
        }
        long applicationCount = applicationRepository.countByCvId(cvId);
        if (applicationCount == 0) {
            try {
                fileUploadService.deleteImage(cv.getFileUrl());
            } catch (Exception e) {
                System.err.println("Lỗi khi xóa file trên Cloudinary: " + e.getMessage());
            }
            cvRepository.delete(cv);
        } else {
            cv.setActive(false);
            cvRepository.save(cv);
        }
    }
}