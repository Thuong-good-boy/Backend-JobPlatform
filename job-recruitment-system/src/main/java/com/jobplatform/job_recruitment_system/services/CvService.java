package com.jobplatform.job_recruitment_system.services;

import com.cloudinary.Cloudinary;
import com.jobplatform.job_recruitment_system.config.CloudinaryConfig;
import com.jobplatform.job_recruitment_system.dtos.dto.*;
import com.jobplatform.job_recruitment_system.dtos.request.CvRequest;
import com.jobplatform.job_recruitment_system.exceptions.AppException;
import com.jobplatform.job_recruitment_system.exceptions.ErrorCode;
import com.jobplatform.job_recruitment_system.models.Application;
import com.jobplatform.job_recruitment_system.models.Candidate;
import com.jobplatform.job_recruitment_system.models.Cv;
import com.jobplatform.job_recruitment_system.models.User;
import com.jobplatform.job_recruitment_system.repositories.ApplicationRepository;
import com.jobplatform.job_recruitment_system.repositories.CandidateRepository;
import com.jobplatform.job_recruitment_system.repositories.CvRepository;
import com.jobplatform.job_recruitment_system.utils.ByteArrayMultipartFile;
import com.jobplatform.job_recruitment_system.utils.LatexUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CvService {

    private final CvRepository cvRepository;
    private final FileUploadService fileUploadService;
    private final AiOcrService aiOcrService;
    private final AiMatchingService aiMatchingService;
    private  final UserService  userService;
    private  final ApplicationRepository applicationRepository;
    private  final CandidateService candidateService;
    private  final CandidateRepository candidateRepository;
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

    public Cv createAndAnalyze(String urlfile) throws Exception {

        Long userId = userService.getCurrentUserId();
        User user = userService.getUserId(userId).orElseThrow(()-> new AppException(ErrorCode.AUTH_008));
        String fileName = urlfile.substring(urlfile.lastIndexOf("/") + 1);
        URL url = new URL(urlfile);
        byte[] fileBytes;
        try (InputStream in = url.openStream()) {
            fileBytes = in.readAllBytes();
        }
        String contentType = urlfile.toLowerCase().endsWith(".pdf") ? "application/pdf" : "image/jpeg";
        System.out.println(urlfile);
        MultipartFile fakeFile = new ByteArrayMultipartFile(fileBytes, "file", fileName, contentType);

        String cvDataJson = aiOcrService.extractCvInfoToJson(fakeFile);
        System.out.println("Dữ liệu JSON trích xuất được:\n" + cvDataJson);
        Cv cv = new Cv();
        cv.setUser(user);
        cv.setFileUrl(urlfile);
        cv.setCvData(cvDataJson);
        cv.setCvName(fileName);
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
                fileUploadService.deleteFile(cv.getFileUrl());
            } catch (Exception e) {
                System.err.println("Lỗi khi xóa file trên Cloudinary: " + e.getMessage());
            }
            cvRepository.delete(cv);
        } else {
            cv.setActive(false);
            cvRepository.save(cv);
        }
    }
    public byte[] generateCvPdf(CvRequest data) throws Exception {


        String sessionId = UUID.randomUUID().toString();
        Path jobDir = Files.createTempDirectory("cv_job_" + sessionId);
        String workDir = jobDir.toAbsolutePath().toString();

        String texFileName = "cv_" + sessionId + ".tex";
        String pdfFileName = "cv_" + sessionId + ".pdf";
        Path texFilePath = jobDir.resolve(texFileName);
        Path pdfFilePath = jobDir.resolve(pdfFileName);
        String templateName = data.getTemplateName();

        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:templates/" + templateName + "/*.*");
            for (Resource res : resources) {
                if (res.getFilename() != null) {
                    Path targetPath = jobDir.resolve(res.getFilename());
                    try (InputStream is = res.getInputStream()) {
                        Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }

            Path baseTexPath = jobDir.resolve("template.tex");
            if (!Files.exists(baseTexPath)) {
                throw new RuntimeException("Không tìm thấy file template.tex cho mẫu: " + templateName);
            }

            String latexContent = Files.readString(baseTexPath, StandardCharsets.UTF_8);

            boolean hasSkills = data.getSkills() != null && !data.getSkills().isEmpty();
            boolean hasExp = data.getExperiences() != null && !data.getExperiences().isEmpty();
            boolean hasProjects = data.getProjects() != null && !data.getProjects().isEmpty();
            boolean hasEdu = data.getEducations() != null && !data.getEducations().isEmpty();
            boolean hasLanguages = data.getLanguages() != null && !data.getLanguages().isEmpty();
            boolean hasAchievements = data.getAchievements() != null && !data.getAchievements().isEmpty();
            boolean hasPhilosophy = data.getPhilosophy() != null && !data.getPhilosophy().trim().isEmpty();
            boolean hasDayOfLife = data.getDayOfLife() != null && !data.getDayOfLife().isEmpty();
            boolean hasPublications = data.getPublications() != null && !data.getPublications().isEmpty();

            // 1. XỬ LÝ ẨN/HIỆN CÁC KHỐI LỚN
            latexContent = processBlock(latexContent, "philosophy", hasPhilosophy);
            latexContent = processBlock(latexContent, "dayOfLife", hasDayOfLife);
            latexContent = processBlock(latexContent, "publications", hasPublications);
            latexContent = processBlock(latexContent, "experiences", hasExp);
            latexContent = processBlock(latexContent, "projects", hasProjects);
            latexContent = processBlock(latexContent, "skills", hasSkills);
            latexContent = processBlock(latexContent, "achievements", hasAchievements);
            latexContent = processBlock(latexContent, "languages", hasLanguages);
            latexContent = processBlock(latexContent, "educations", hasEdu);

            // 2. TẢI VÀ ĐỊNH DẠNG AVATAR (Chỉ giữ định dạng mẫu AltaCV)
            String avatarCmd = "";
            String downloadedAvatarName = null;
            String avatarUrl = data.getAvatarUrl();

            if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
                downloadedAvatarName = downloadAvatar(avatarUrl, workDir, sessionId);
                if (downloadedAvatarName != null) {
                    avatarCmd = "\\photoR{2.8cm}{" + downloadedAvatarName + "}";
                }
            }
            latexContent = latexContent.replace("{{AVATAR_CMD}}", avatarCmd);

            // 3. THAY THẾ CÁC TRƯỜNG THÔNG TIN CÁ NHÂN CƠ BẢN
            latexContent = latexContent.replace("{{FullName}}", LatexUtils.escapeLatex(data.getFullName() != null ? data.getFullName() : ""));
            latexContent = latexContent.replace("{{jobTitle}}", LatexUtils.escapeLatex(data.getJobTitle() != null ? data.getJobTitle() : ""));
            latexContent = latexContent.replace("{{address}}", LatexUtils.escapeLatex(data.getAddress() != null ? data.getAddress() : ""));

            // Xử lý mạng xã hội hỗ trợ cả 2 định dạng placeholder cũ và mới của AltaCV
            String githubVal = data.getGithub() != null ? data.getGithub() : "";
            latexContent = latexContent.replace("{{github_text}}", LatexUtils.escapeLatex(githubVal));
            latexContent = latexContent.replace("{{github_raw}}", githubVal);
            latexContent = latexContent.replace("{{github}}", LatexUtils.escapeLatex(githubVal));

            String linkedinVal = data.getLinkedin() != null ? data.getLinkedin() : "";
            latexContent = latexContent.replace("{{linkedin_text}}", LatexUtils.escapeLatex(linkedinVal));
            latexContent = latexContent.replace("{{linkedin_raw}}", linkedinVal);
            latexContent = latexContent.replace("{{linkedin}}", LatexUtils.escapeLatex(linkedinVal));

            String gitlabVal = data.getGitlab() != null ? data.getGitlab() : "";
            latexContent = latexContent.replace("{{gitlab_text}}", LatexUtils.escapeLatex(gitlabVal));
            latexContent = latexContent.replace("{{gitlab_raw}}", gitlabVal);
            latexContent = latexContent.replace("{{gitlab}}", LatexUtils.escapeLatex(gitlabVal));

            latexContent = latexContent.replace("{{email}}", LatexUtils.escapeLatex(data.getEmail() != null ? data.getEmail() : ""));
            latexContent = latexContent.replace("{{phone}}", LatexUtils.escapeLatex(data.getPhone() != null ? data.getPhone() : ""));
            latexContent = latexContent.replace("{{twitter}}", LatexUtils.escapeLatex(data.getTwitter() != null ? data.getTwitter() : ""));
            latexContent = latexContent.replace("{{orcid}}", LatexUtils.escapeLatex(data.getOrcid() != null ? data.getOrcid() : ""));
            latexContent = latexContent.replace("{{homepage}}", data.getHomepage() != null ? data.getHomepage() : "");
            latexContent = latexContent.replace("{{{philosophy}}}", LatexUtils.escapeLatex(data.getPhilosophy() != null ? data.getPhilosophy() : ""));


            // 4.1 Khối SKILLS
            if (hasSkills) {
                StringBuilder skillBuilder = new StringBuilder();
                for (String skill : data.getSkills()) {
                    skillBuilder.append("\\cvtag{").append(LatexUtils.escapeLatex(skill)).append("} \n");
                }
                latexContent = latexContent.replace("{{skills_LIST}}", skillBuilder.toString());
            }

            // 4.2 Khối EXPERIENCES
            if (hasExp) {
                // 1. Sắp xếp an toàn: Tránh lỗi NullPointerException nếu kinh nghiệm hoặc duration bị null
                data.getExperiences().sort((e1, e2) -> {
                    if (e1 == null || e1.getDuration() == null) return 1;
                    if (e2 == null || e2.getDuration() == null) return -1;
                    return parseEndDate(e2.getDuration()).compareTo(parseEndDate(e1.getDuration()));
                });

                // 2. Đã sửa lỗi đánh máy "String   Builder" thành "StringBuilder" chuẩn
                StringBuilder expBuilder = new StringBuilder();

                for (int i = 0; i < data.getExperiences().size(); i++) {
                    ExperienceDto exp = data.getExperiences().get(i);
                    if (exp == null) continue; // Bỏ qua phần tử null

                    expBuilder.append("\\cvevent{")
                            .append(LatexUtils.escapeLatex(exp.getRole() != null ? exp.getRole() : "")).append("}{")
                            .append(LatexUtils.escapeLatex(exp.getCompany() != null ? exp.getCompany() : "")).append("}{")
                            .append(LatexUtils.escapeLatex(exp.getDuration() != null ? exp.getDuration() : "")).append("}{")
                            .append(LatexUtils.escapeLatex(exp.getLocation() != null ? exp.getLocation() : "")).append("}\n");

                    if (exp.getDescription() != null && !exp.getDescription().isEmpty()) {
                        expBuilder.append("\\begin{itemize}\n");
                        for (String descItem : exp.getDescription()) {
                            if (descItem != null && !descItem.trim().isEmpty()) {
                                expBuilder.append("  \\item ").append(LatexUtils.escapeLatex(descItem)).append("\n");
                            }
                        }
                        expBuilder.append("\\end{itemize}\n");
                    }
                    if (i < data.getExperiences().size() - 1) {
                        expBuilder.append("\\divider\n");
                    }
                    expBuilder.append("\n");
                }
                latexContent = latexContent.replace("{{experiences_LIST}}", expBuilder.toString());
            }

            if (hasProjects) {
                data.getProjects().sort((p1, p2) -> {
                    if (p1 == null || p1.getDuration() == null) return 1;
                    if (p2 == null || p2.getDuration() == null) return -1;
                    return parseEndDate(p2.getDuration()).compareTo(parseEndDate(p1.getDuration()));
                });

                StringBuilder projBuilder = new StringBuilder();
                for (int i = 0; i < data.getProjects().size(); i++) {
                    ProjectDto proj = data.getProjects().get(i);
                    if (proj == null) continue;

                    projBuilder.append("\\cvevent{")
                            .append(LatexUtils.escapeLatex(proj.getRole() != null ? proj.getRole() : "")).append("}{")
                            .append(LatexUtils.escapeLatex(proj.getName() != null ? proj.getName() : "")).append("}{")
                            .append(LatexUtils.escapeLatex(proj.getDuration() != null ? proj.getDuration() : "")).append("}{}\n");

                    if (proj.getDescription() != null && !proj.getDescription().isEmpty()) {
                        projBuilder.append("\\begin{itemize}\n");
                        for (String descItem : proj.getDescription()) {
                            if (descItem != null && !descItem.trim().isEmpty()) {
                                projBuilder.append("  \\item ").append(LatexUtils.escapeLatex(descItem)).append("\n");
                            }
                        }
                        projBuilder.append("\\end{itemize}\n");
                    }
                    if (i < data.getProjects().size() - 1) {
                        projBuilder.append("\\divider\n");
                    }
                    projBuilder.append("\n");
                }
                latexContent = latexContent.replace("{{projects_LIST}}", projBuilder.toString());
            }

            // 4.4 Khối EDUCATIONS
            if (hasEdu) {
                StringBuilder eduBuilder = new StringBuilder();
                for (int i = 0; i < data.getEducations().size(); i++) {
                    EducationDto edu = data.getEducations().get(i);
                    eduBuilder.append("\\cvevent{")
                            .append(LatexUtils.escapeLatex(edu.getDegree())).append("}{")
                            .append(LatexUtils.escapeLatex(edu.getUniversity())).append("}{")
                            .append(LatexUtils.escapeLatex(edu.getDuration())).append("}{}\n");

                    if (edu.getDetails() != null && !edu.getDetails().trim().isEmpty()) {
                        eduBuilder.append(LatexUtils.escapeLatex(edu.getDetails())).append("\n");
                    }
                    if (i < data.getEducations().size() - 1) {
                        eduBuilder.append("\\divider\n");
                    }
                    eduBuilder.append("\n");
                }
                latexContent = latexContent.replace("{{educations_LIST}}", eduBuilder.toString());
            }

            // 4.5 Khối LANGUAGES
            if (hasLanguages) {
                StringBuilder langBuilder = new StringBuilder();
                for (int i = 0; i < data.getLanguages().size(); i++) {
                    LanguageDto lang = data.getLanguages().get(i);
                    langBuilder.append("\\cvskill{")
                            .append(LatexUtils.escapeLatex(lang.getLanguage())).append("}{")
                            .append(lang.getLevel()).append("}\n");

                    if (i < data.getLanguages().size() - 1) {
                        langBuilder.append("\\divider\n");
                    }
                }
                latexContent = latexContent.replace("{{languages_LIST}}", langBuilder.toString());
            }

            // 4.6 Khối ACHIEVEMENTS
            if (hasAchievements) {
                StringBuilder achBuilder = new StringBuilder();
                for (int i = 0; i < data.getAchievements().size(); i++) {
                    AchievementDto ach = data.getAchievements().get(i);
                    String iconStyle = (ach.getIcon() != null && !ach.getIcon().trim().isEmpty()) ? ach.getIcon() : "\\faTrophy";
                    achBuilder.append("\\cvachievement{").append(iconStyle).append("}{")
                            .append(LatexUtils.escapeLatex(ach.getTitle())).append("}{")
                            .append(LatexUtils.escapeLatex(ach.getDetails())).append("}\n");

                    if (i < data.getAchievements().size() - 1) {
                        achBuilder.append("\\divider\n");
                    }
                }
                latexContent = latexContent.replace("{{achievements_LIST}}", achBuilder.toString());
            }

            // 4.7 Khối DAY OF LIFE (Vòng xoay phân bổ thời gian - Chỉ AltaCV có)
            if (hasDayOfLife) {
                StringBuilder wheelBuilder = new StringBuilder();
                for (WheelChartDto chart : data.getDayOfLife()) {
                    String textWidth = (chart.getTextWidth() != null) ? chart.getTextWidth() : "8em";
                    String color = (chart.getColor() != null) ? chart.getColor() : "accent!60";

                    wheelBuilder.append("  ")
                            .append(chart.getHours()).append("/")
                            .append(textWidth).append("/")
                            .append(color).append("/")
                            .append(LatexUtils.escapeLatex(chart.getText())).append(",\n");
                }
                String wheelString = wheelBuilder.toString().trim();
                if (wheelString.endsWith(",")) {
                    wheelString = wheelString.substring(0, wheelString.length() - 1);
                }
                latexContent = latexContent.replace("{{dayOfLife_LIST}}", wheelString);
            }

            if (hasPublications) {
                StringBuilder pubBuilder = new StringBuilder();
                for (int i = 0; i < data.getPublications().size(); i++) {
                    PublicationDto pub = data.getPublications().get(i);
                    pubBuilder.append("\\cvevent{")
                            .append(LatexUtils.escapeLatex(pub.getTitle())).append("}{")
                            .append(LatexUtils.escapeLatex(pub.getAuthors())).append("}{")
                            .append(LatexUtils.escapeLatex(pub.getYear())).append("}{")
                            .append(LatexUtils.escapeLatex(pub.getPublisher())).append("}\n");

                    if (i < data.getPublications().size() - 1) {
                        pubBuilder.append("\\divider\n");
                    }
                }
                latexContent = latexContent.replace("{{publications_LIST}}", pubBuilder.toString());
            }

            // 5. DỌN DẸP SẠCH CÁC TOKEN THỪA KHÔNG ĐƯỢC ĐIỀN DỮ LIỆU
            latexContent = latexContent
                    .replace("{{BEGIN_philosophy}}", "").replace("{{END_philosophy}}", "")
                    .replace("{{BEGIN_dayOfLife}}", "").replace("{{END_dayOfLife}}", "")
                    .replace("{{BEGIN_publications}}", "").replace("{{END_publications}}", "")
                    .replace("{{BEGIN_experiences}}", "").replace("{{END_experiences}}", "")
                    .replace("{{BEGIN_projects}}", "").replace("{{END_projects}}", "")
                    .replace("{{BEGIN_skills}}", "").replace("{{END_skills}}", "")
                    .replace("{{BEGIN_achievements}}", "").replace("{{END_achievements}}", "")
                    .replace("{{BEGIN_languages}}", "").replace("{{END_languages}}", "")
                    .replace("{{BEGIN_educations}}", "").replace("{{END_educations}}", "");

            latexContent = latexContent
                    .replace("{{fullName}}", "").replace("{{jobTitle}}", "")
                    .replace("{{email}}", "").replace("{{phone}}", "").replace("{{address}}", "")
                    .replace("{{github}}", "").replace("{{github_text}}", "").replace("{{github_raw}}", "")
                    .replace("{{linkedin}}", "").replace("{{linkedin_text}}", "").replace("{{linkedin_raw}}", "")
                    .replace("{{gitlab}}", "").replace("{{gitlab_text}}", "").replace("{{gitlab_raw}}", "")
                    .replace("{{homepage}}", "").replace("{{homepage_raw}}", "")
                    .replace("{{twitter}}", "").replace("{{orcid}}", "")
                    .replace("{{{philosophy}}}", "");

            latexContent = latexContent
                    .replace("{{experiences_LIST}}", "")
                    .replace("{{projects_LIST}}", "")
                    .replace("{{skills_LIST}}", "")
                    .replace("{{achievements_LIST}}", "")
                    .replace("{{dayOfLife_LIST}}", "")
                    .replace("{{publications_LIST}}", "")
                    .replace("{{languages_LIST}}", "")
                    .replace("{{educations_LIST}}", "");

            Files.writeString(texFilePath, latexContent, StandardCharsets.UTF_8);

            // LOGGING NỘI DUNG FILE ĐỂ KIỂM TRA TRƯỚC KHI BIÊN DỊCH
            System.out.println("==================================================");
            System.out.println("NỘI DUNG FILE .TEX ALTA_CV TRƯỚC KHI BIÊN DỊCH:");
            System.out.println("==================================================");
            String[] lines = latexContent.split("\\r?\\n");
            for (int i = 0; i < lines.length; i++) {
                System.out.println(String.format("%03d: %s", (i + 1), lines[i]));
            }
            System.out.println("==================================================");

            // 6. TIẾN HÀNH BIÊN DỊCH BẰNG XELATEX
            ProcessBuilder processBuilder = new ProcessBuilder("xelatex", "-interaction=nonstopmode", "-halt-on-error", texFileName);
            processBuilder.directory(jobDir.toFile());
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            StringBuilder outputLog = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    outputLog.append(line).append("\n");
                }
            }

            boolean finished = process.waitFor(45, TimeUnit.SECONDS);
            if (!finished) {
                process.destroy();
                throw new RuntimeException("Quá thời gian biên dịch PDF (Timeout 45s)");
            }

            if (process.exitValue() != 0) {
                System.err.println("=== LỖI BIÊN DỊCH LATEX ===");
                System.err.println(outputLog.toString());
                throw new RuntimeException("Lỗi cú pháp khi biên dịch LaTeX AltaCV!");
            }

            if (Files.exists(pdfFilePath)) {
                return Files.readAllBytes(pdfFilePath);
            } else {
                throw new RuntimeException("Không tìm thấy file PDF đầu ra.");
            }

        } finally {
            try (java.util.stream.Stream<Path> walk = Files.walk(jobDir)) {
                walk.sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
                    try { Files.deleteIfExists(path); } catch (IOException e) { }
                });
                fileUploadService.deleteImage(data.getAvatarUrl());
            } catch (IOException e) { }
        }
    }
    public byte[] generateResumePdf(CvRequest data) throws Exception {

        if (data != null) {
            System.out.println("=== BIÊN DỊCH MẪU RESUME ===");
            System.out.println("-> Họ tên: " + data.getFullName());
            System.out.println("-> Email: " + data.getEmail());
            System.out.println("-> Ảnh : " + data.getAvatarUrl());
            System.out.println("-> Tên Template sử dụng: " + data.getTemplateName());
        } else {
            throw new IllegalArgumentException("Dữ liệu CV (Resume) không được để trống (NULL)!");
        }

        String sessionId = UUID.randomUUID().toString();
        Path jobDir = Files.createTempDirectory("resume_job_" + sessionId);
        String workDir = jobDir.toAbsolutePath().toString();

        String texFileName = "resume_" + sessionId + ".tex";
        String pdfFileName = "resume_" + sessionId + ".pdf";
        Path texFilePath = jobDir.resolve(texFileName);
        Path pdfFilePath = jobDir.resolve(pdfFileName);
        String templateName = data.getTemplateName();

        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:templates/" + templateName + "/*.*");
            for (Resource res : resources) {
                if (res.getFilename() != null) {
                    Path targetPath = jobDir.resolve(res.getFilename());
                    try (InputStream is = res.getInputStream()) {
                        Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }

            Path baseTexPath = jobDir.resolve("template.tex");
            if (!Files.exists(baseTexPath)) {
                throw new RuntimeException("Không tìm thấy file template.tex cho mẫu Resume: " + templateName);
            }

            String latexContent = Files.readString(baseTexPath, StandardCharsets.UTF_8);

            // Kiểm tra sự tồn tại của dữ liệu từng khối lớn
            boolean hasSkills = data.getSkills() != null && !data.getSkills().isEmpty();
            boolean hasExp = data.getExperiences() != null && !data.getExperiences().isEmpty();
            boolean hasProjects = data.getProjects() != null && !data.getProjects().isEmpty();
            boolean hasEdu = data.getEducations() != null && !data.getEducations().isEmpty();
            boolean hasPhilosophy = data.getPhilosophy() != null && !data.getPhilosophy().trim().isEmpty();

            // 1. XỬ LÝ ẨN/HIỆN CÁC KHỐI LỚN THEO MẪU MỚI
            latexContent = processBlock(latexContent, "philosophy", hasPhilosophy);
            latexContent = processBlock(latexContent, "experiences", hasExp);
            latexContent = processBlock(latexContent, "projects", hasProjects);
            latexContent = processBlock(latexContent, "skills", hasSkills);
            latexContent = processBlock(latexContent, "educations", hasEdu);

            // 2. TẢI VÀ ĐỊNH DẠNG AVATAR CHO MẪU RESUME (Dùng \includegraphics)
            String avatarCmd = "";
            String downloadedAvatarName = null;
            String avatarUrl = data.getAvatarUrl();

            if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
                downloadedAvatarName = downloadAvatar(avatarUrl, workDir, sessionId);
                if (downloadedAvatarName != null) {
                    avatarCmd = "\\includegraphics[width=3cm]{" + downloadedAvatarName + "}";
                }
            }
            latexContent = latexContent.replace("{{AVATAR_CMD}}", avatarCmd);

            // 3. THAY THẾ CÁC TRƯỜNG THÔNG TIN CÁ NHÂN CƠ BẢN
            latexContent = latexContent.replace("{{FullName}}", LatexUtils.escapeLatex(data.getFullName() != null ? data.getFullName() : ""));
            latexContent = latexContent.replace("{{jobTitle}}", LatexUtils.escapeLatex(data.getJobTitle() != null ? data.getJobTitle() : ""));
            latexContent = latexContent.replace("{{address}}", LatexUtils.escapeLatex(data.getAddress() != null ? data.getAddress() : ""));
            latexContent = latexContent.replace("{{phone}}", LatexUtils.escapeLatex(data.getPhone() != null ? data.getPhone() : ""));
            latexContent = latexContent.replace("{{email}}", LatexUtils.escapeLatex(data.getEmail() != null ? data.getEmail() : ""));
            latexContent = latexContent.replace("{{{philosophy}}}", LatexUtils.escapeLatex(data.getPhilosophy() != null ? data.getPhilosophy() : ""));

            StringBuilder socialBuilder = new StringBuilder();

            if (data.getGithub() != null && !data.getGithub().trim().isEmpty()) {
                socialBuilder.append("\\newline GitHub: \\href{").append(data.getGithub()).append("}{").append(LatexUtils.escapeLatex(data.getGithub())).append("}");
            }
            if (data.getLinkedin() != null && !data.getLinkedin().trim().isEmpty()) {
                socialBuilder.append("\\newline LinkedIn: \\href{").append(data.getLinkedin()).append("}{").append(LatexUtils.escapeLatex(data.getLinkedin())).append("}");
            }
            if (data.getGitlab() != null && !data.getGitlab().trim().isEmpty()) {
                socialBuilder.append("\\newline GitLab: \\href{").append(data.getGitlab()).append("}{").append(LatexUtils.escapeLatex(data.getGitlab())).append("}");
            }

            latexContent = latexContent.replace("{{social_networks_block}}", socialBuilder.toString());

            if (hasSkills) {
                StringBuilder skillBuilder = new StringBuilder();
                skillBuilder.append("\\item \\textbf{Kỹ năng chuyên môn:} \\newline {\\footnotesize ");
                for (int i = 0; i < data.getSkills().size(); i++) {
                    skillBuilder.append(LatexUtils.escapeLatex(data.getSkills().get(i)));
                    if (i < data.getSkills().size() - 1) {
                        skillBuilder.append(", ");
                    }
                }
                skillBuilder.append("}\n");
                latexContent = latexContent.replace("{{skills_LIST}}", skillBuilder.toString());
            }

            // 4.2 Khối EXPERIENCES (Dùng cấu trúc lề \hfill và dấu bullet toán học $\bullet$)
            if (hasExp) {
                data.getExperiences().sort((e1, e2) -> parseEndDate(e2.getDuration()).compareTo(parseEndDate(e1.getDuration())));
                StringBuilder expBuilder = new StringBuilder();
                for (int i = 0; i < data.getExperiences().size(); i++) {
                    ExperienceDto exp = data.getExperiences().get(i);

                    expBuilder.append("\\item \\textbf{").append(LatexUtils.escapeLatex(exp.getCompany())).append("}")
                            .append(" \\hfill {").append(LatexUtils.escapeLatex(exp.getDuration())).append("}\\\\ \n")
                            .append("\\textit{").append(LatexUtils.escapeLatex(exp.getRole())).append("} ");

                    if (exp.getLocation() != null && !exp.getLocation().isEmpty()) {
                        expBuilder.append(" -- \\textit{").append(LatexUtils.escapeLatex(exp.getLocation())).append("}");
                    }
                    expBuilder.append("\\\\[0.1cm] \n");

                    if (exp.getDescription() != null && !exp.getDescription().isEmpty()) {
                        for (int j = 0; j < exp.getDescription().size(); j++) {
                            String descItem = exp.getDescription().get(j);
                            if (descItem != null && !descItem.trim().isEmpty()) {
                                expBuilder.append("$\\bullet$ ").append(LatexUtils.escapeLatex(descItem));
                                if (j < exp.getDescription().size() - 1) {
                                    expBuilder.append("\\\\ \n");
                                } else {
                                    expBuilder.append("\n");
                                }
                            }
                        }
                    }
                    expBuilder.append("\\vspace{0.1cm}\n");
                }
                latexContent = latexContent.replace("{{experiences_LIST}}", expBuilder.toString());
            }

            // 4.3 Khối PROJECTS
            if (hasProjects) {
                data.getProjects().sort((p1, p2) -> parseEndDate(p2.getDuration()).compareTo(parseEndDate(p1.getDuration())));
                StringBuilder projBuilder = new StringBuilder();
                for (int i = 0; i < data.getProjects().size(); i++) {
                    ProjectDto proj = data.getProjects().get(i);

                    projBuilder.append("\\item \\textbf{").append(LatexUtils.escapeLatex(proj.getName())).append("}")
                            .append(" \\hfill {").append(LatexUtils.escapeLatex(proj.getDuration())).append("}\\\\ \n")
                            .append("\\textit{Vai trò: ").append(LatexUtils.escapeLatex(proj.getRole())).append("}\\\\[0.1cm] \n");

                    if (proj.getDescription() != null && !proj.getDescription().isEmpty()) {
                        for (int j = 0; j < proj.getDescription().size(); j++) {
                            String descItem = proj.getDescription().get(j);
                            if (descItem != null && !descItem.trim().isEmpty()) {
                                projBuilder.append("$\\bullet$ ").append(LatexUtils.escapeLatex(descItem));
                                if (j < proj.getDescription().size() - 1) {
                                    projBuilder.append("\\\\ \n");
                                } else {
                                    projBuilder.append("\n");
                                }
                            }
                        }
                    }
                    projBuilder.append("\\vspace{0.1cm}\n");
                }
                latexContent = latexContent.replace("{{projects_LIST}}", projBuilder.toString());
            }

            // 4.4 Khối EDUCATIONS
            if (hasEdu) {
                StringBuilder eduBuilder = new StringBuilder();
                for (int i = 0; i < data.getEducations().size(); i++) {
                    EducationDto edu = data.getEducations().get(i);

                    eduBuilder.append("\\item \\textbf{").append(LatexUtils.escapeLatex(edu.getUniversity())).append("}")
                            .append(" \\hfill {").append(LatexUtils.escapeLatex(edu.getDuration())).append("}\\\\ \n")
                            .append("\\textit{").append(LatexUtils.escapeLatex(edu.getDegree())).append("} \\\\ \n");

                    if (edu.getDetails() != null && !edu.getDetails().trim().isEmpty()) {
                        eduBuilder.append("{\\small ").append(LatexUtils.escapeLatex(edu.getDetails())).append("} \\\\ \n");
                    }
                    eduBuilder.append("\\vspace{0.1cm}\n");
                }
                latexContent = latexContent.replace("{{educations_LIST}}", eduBuilder.toString());
            }

            // 5. XÓA SẠCH TOKEN THỪA KHÔNG CÓ TRONG KẾT QUẢ ĐIỀN DATA
            latexContent = latexContent
                    .replace("{{BEGIN_philosophy}}", "").replace("{{END_philosophy}}", "")
                    .replace("{{BEGIN_experiences}}", "").replace("{{END_experiences}}", "")
                    .replace("{{BEGIN_projects}}", "").replace("{{END_projects}}", "")
                    .replace("{{BEGIN_skills}}", "").replace("{{END_skills}}", "")
                    .replace("{{BEGIN_educations}}", "").replace("{{END_educations}}", "");

            latexContent = latexContent
                    .replace("{{FullName}}", "").replace("{{jobTitle}}", "").replace("{{address}}", "")
                    .replace("{{phone}}", "").replace("{{email}}", "").replace("{{social_networks_block}}", "")
                    .replace("{{{philosophy}}}", "");

            latexContent = latexContent
                    .replace("{{experiences_LIST}}", "")
                    .replace("{{projects_LIST}}", "")
                    .replace("{{skills_LIST}}", "")
                    .replace("{{educations_LIST}}", "");

            Files.writeString(texFilePath, latexContent, StandardCharsets.UTF_8);

            // LOGGING NỘI DUNG ĐỂ TIỆN MONITORING TRONG CONSOLE
            System.out.println("==================================================");
            System.out.println("NỘI DUNG FILE .TEX RESUME TRƯỚC KHI BIÊN DỊCH:");
            System.out.println("==================================================");
            String[] lines = latexContent.split("\\r?\\n");
            for (int i = 0; i < lines.length; i++) {
                System.out.println(String.format("%03d: %s", (i + 1), lines[i]));
            }
            System.out.println("==================================================");

            // 6. TIẾN HÀNH BIÊN DỊCH BẰNG XELATEX
            ProcessBuilder processBuilder = new ProcessBuilder("xelatex", "-interaction=nonstopmode", "-halt-on-error", texFileName);
            processBuilder.directory(jobDir.toFile());
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            StringBuilder outputLog = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    outputLog.append(line).append("\n");
                }
            }

            boolean finished = process.waitFor(45, TimeUnit.SECONDS);
            if (!finished) {
                process.destroy();
                throw new RuntimeException("Quá thời gian biên dịch PDF Resume (Timeout 45s)");
            }

            if (process.exitValue() != 0) {
                System.err.println("=== LỖI BIÊN DỊCH LATEX (RESUME) ===");
                System.err.println(outputLog.toString());
                throw new RuntimeException("Lỗi cú pháp khi biên dịch mẫu Resume!");
            }

            if (Files.exists(pdfFilePath)) {
                return Files.readAllBytes(pdfFilePath);
            } else {
                throw new RuntimeException("Không tìm thấy file PDF đầu ra của mẫu Resume.");
            }

        } finally {
            // Dọn dẹp folder tmp
            try (java.util.stream.Stream<Path> walk = Files.walk(jobDir)) {
                walk.sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
                    try { Files.deleteIfExists(path); } catch (IOException e) { }
                });
                fileUploadService.deleteImage(data.getAvatarUrl());
            } catch (IOException e) { }
        }
    }
    private String processBlock(String latexContent, String blockName, boolean keep) {
        String regex = "(?is)\\{\\{BEGIN_" + blockName + "\\}\\}(.*?)\\{\\{END_" + blockName + "\\}\\}";
        if (keep) {
            return latexContent.replaceAll(regex, "$1");
        } else {
            return latexContent.replaceAll(regex, "");
        }
    }
    private String downloadAvatar(String imageUrl, String workDir, String uuid) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return null;
        }
        try {
            String fileName = "avatar_" + uuid + ".png";
            Path targetPath = Paths.get(workDir, fileName);
            URL url = new URL(imageUrl);

            java.net.URLConnection connection = url.openConnection();
            connection.setConnectTimeout(5000); // 5 giây kết nối
            connection.setReadTimeout(5000);    // 5 giây đọc dữ liệu

            try (InputStream in = connection.getInputStream()) {
                Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
            return fileName;
        } catch (Exception e) {
            System.err.println("Lỗi khi tải avatar từ Cloudinary: " + e.getMessage());
            return null;
        }
    }
    private java.time.YearMonth parseEndDate(String duration) {
        java.time.YearMonth defaultMin = java.time.YearMonth.of(1900, 1);

        if (duration == null || !duration.contains("-")) {
            return defaultMin;
        }
        try {
            String[] parts = duration.split("-");
            if (parts.length < 2) return defaultMin;

            String endDateStr = parts[1].trim().toLowerCase();

            if (endDateStr.contains("hiện tại") || endDateStr.contains("present") || endDateStr.contains("now") || endDateStr.isEmpty()) {
                return java.time.YearMonth.now();
            }

            // 2. Nếu người dùng chỉ nhập mỗi năm (Ví dụ: "2023") -> Tự động hiểu là tháng 12 năm đó "12/2023"
            if (endDateStr.matches("^\\d{4}$")) {
                return java.time.YearMonth.of(Integer.parseInt(endDateStr), 12);
            }

            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MM/yyyy");

            return java.time.YearMonth.parse(endDateStr, formatter);
        } catch (Exception e) {
            // Log nhẹ lỗi ra console để debug khi cần thiết
            System.err.println("Lỗi parse chuỗi duration: " + duration + " -> Đưa về default 1900-01");
            return defaultMin;
        }
    }
    public String feedBack(Long cvId) throws Exception {
        Long userId = userService.getCurrentUserId();
        Candidate candidate = candidateService.getProfile(userId);

        if (candidate.getAiPoints() <= 0) {
            throw new RuntimeException("Bạn đã hết lượt sử dụng AI!");
        }

        candidate.setAiPoints(candidate.getAiPoints() - 1);
        candidateRepository.save(candidate);

        Cv cv = cvRepository.findById(cvId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy CV với ID: " + cvId));

        String cvDataJson = cv.getCvData().toString();

        return aiOcrService.evaluateCvFromJsonText(cvDataJson);
    }

}