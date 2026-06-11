package com.jobplatform.job_recruitment_system.controllers;

import com.cloudinary.Url;
import com.jobplatform.job_recruitment_system.dtos.request.AutoFixRequest;
import com.jobplatform.job_recruitment_system.dtos.request.CvRequest;
import com.jobplatform.job_recruitment_system.models.Candidate;
import com.jobplatform.job_recruitment_system.models.Cv;

import com.jobplatform.job_recruitment_system.models.User;
import com.jobplatform.job_recruitment_system.services.*;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/cvs")
@RequiredArgsConstructor
public class CvController {

    private final CvService cvService;
    private final UserService userService;
    private  final CandidateService candidateService;
    private  final FileUploadService fileUploadService;
    private  final AiOcrService aiOcrService;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(
            @RequestParam("file")
                    @NotNull(message = "FILE_REQUIRED")
            MultipartFile file) throws  Exception {

            return ResponseEntity.ok(cvService.uploadAndAnalyze(file));

    }

    @GetMapping("/my-cvs")
    public ResponseEntity<?> getList() {
        List<Cv> cvList = cvService.getMyActiveCvs();
        Integer aiPoints = candidateService.getAiPoints();
        return ResponseEntity.ok(Map.of("aiPoints", aiPoints,"cvList", cvList));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
            cvService.softDelete(id);
            return ResponseEntity.ok(Map.of("message", "Đã xóa hồ sơ thành công"));
    }
    @PostMapping(value ="/generate",produces = "application/json;charset=UTF-8")
    public ResponseEntity<?> generateCv(@RequestBody CvRequest request) {
        try {
            byte[] pdfBytes = request.getTemplateName().equals("AltaCV")? cvService.generateCvPdf(request): cvService.generateResumePdf(request);
            String cloudinaryUrl = fileUploadService.uploadPdfBytes(pdfBytes);
            System.out.println(cloudinaryUrl);
            return ResponseEntity.ok().body(Map.of(
                    "status", "success",
                 "url", cloudinaryUrl
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Không thể tạo và upload CV"
            ));
        }
    }
    @PostMapping("/update-logo")
    public ResponseEntity<?>  updateLogo(
            @RequestParam("logo") MultipartFile file
    ){
        try {
            String url = fileUploadService.uploadFile(file);
            return ResponseEntity.ok(url);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Không thể updateLogo"
            ));
        }
    }
    @PostMapping("/delete-url-cv")
    public void deleteurlCV(@RequestBody Map<String, String> payload) {
        String url = payload.get("pdfUrl");
        fileUploadService.deleteFile(url);
    }
    @PostMapping("/" +
            "save-url-cv")
    public ResponseEntity<?> saveurlCV(@RequestBody Map<String, String> payload) {
        try {
            String pdfUrl = payload.get("pdfUrl");

            cvService.createAndAnalyze(pdfUrl);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
    @PostMapping("/generate-pdf-with-ai")
    public ResponseEntity<?> generatePdfWithAi(@RequestBody AutoFixRequest request) throws Exception {
        try {
            CvRequest upgradedRequest = aiOcrService.rewriteCvData(request);
            upgradedRequest.setTemplateName(request.getTemplateKey());
            System.out.println(upgradedRequest.getTemplateName());
            upgradedRequest.setAvatarUrl(request.getAvatarUrl());
            byte[] pdfBytes = cvService.generateCvPdf(upgradedRequest);
            String cloudinaryUrl = fileUploadService.uploadPdfBytes(pdfBytes);
            System.out.println(cloudinaryUrl);
            return ResponseEntity.ok().body(Map.of(
                    "status", "success",
                    "url", cloudinaryUrl
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Không thể tạo và upload CV"
            ));
        }
    }
    @PostMapping("/save-cv")
    public ResponseEntity<?> saveCV(@RequestBody CvRequest finalRequest) {
        try {
            byte[] finalPdfBytes = cvService.generateCvPdf(finalRequest);
            String cloudinaryUrl = fileUploadService.uploadPdfBytes(finalPdfBytes);
            cvService.createAndAnalyze(cloudinaryUrl);
            return ResponseEntity.ok().body(Map.of("status", "success", "url", cloudinaryUrl));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

}