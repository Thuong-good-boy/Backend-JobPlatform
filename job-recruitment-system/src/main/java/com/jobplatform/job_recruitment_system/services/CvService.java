package com.jobplatform.job_recruitment_system.services;

import com.cloudinary.Cloudinary;
import com.jobplatform.job_recruitment_system.config.CloudinaryConfig;
import com.jobplatform.job_recruitment_system.dtos.dto.*;
import com.jobplatform.job_recruitment_system.dtos.request.CvRequest;
import com.jobplatform.job_recruitment_system.exceptions.AppException;
import com.jobplatform.job_recruitment_system.exceptions.ErrorCode;
import com.jobplatform.job_recruitment_system.models.Application;
import com.jobplatform.job_recruitment_system.models.Cv;
import com.jobplatform.job_recruitment_system.models.User;
import com.jobplatform.job_recruitment_system.repositories.ApplicationRepository;
import com.jobplatform.job_recruitment_system.repositories.CvRepository;
import com.jobplatform.job_recruitment_system.utils.ByteArrayMultipartFile;
import com.jobplatform.job_recruitment_system.utils.LatexUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
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
                    // Copy asset và cls vào thư mục riêng của session
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

            // Kiểm tra trạng thái dữ liệu đầu vào
            boolean hasSkills = data.getSkills() != null && !data.getSkills().isEmpty();
            boolean hasExp = data.getExperiences() != null && !data.getExperiences().isEmpty();
            boolean hasProjects = data.getProjects() != null && !data.getProjects().isEmpty();
            boolean hasEdu = data.getEducations() != null && !data.getEducations().isEmpty();
            boolean hasLanguages = data.getLanguages() != null && !data.getLanguages().isEmpty();
            boolean hasAchievements = data.getAchievements() != null && !data.getAchievements().isEmpty();
            boolean hasReferees = data.getReferees() != null && !data.getReferees().isEmpty();

            // TỰ ĐỘNG PHÂN BIỆT LOẠI TEMPLATE ĐỂ ĐỔ CÚ PHÁP PHÙ HỢP
            boolean isResumeTemplate = templateName != null && templateName.toLowerCase().contains("resume");

            // 1. XỬ LÝ ẨN/HIỆN CÁC KHỐI LỚN
            latexContent = processBlock(latexContent, "DAYOFLIFE", false);
            latexContent = processBlock(latexContent, "PUBLICATIONS", false);
            latexContent = processBlock(latexContent, "PHILOSOPHY", false);
            latexContent = processBlock(latexContent, "SKILLS", hasSkills);
            latexContent = processBlock(latexContent, "EXPERIENCES", hasExp);
            latexContent = processBlock(latexContent, "PROJECTS", hasProjects);
            latexContent = processBlock(latexContent, "EDUCATIONS", hasEdu);
            latexContent = processBlock(latexContent, "LANGUAGES", hasLanguages);
            latexContent = processBlock(latexContent, "ACHIEVEMENTS", hasAchievements);
            latexContent = processBlock(latexContent, "REFEREES", hasReferees);

            // 2. THAY THẾ CÁC TRƯỜNG THÔNG TIN CÁ NHÂN CƠ BẢN
            latexContent = latexContent.replace("{{FULL_NAME}}", LatexUtils.escapeLatex(data.getFullName()));
            latexContent = latexContent.replace("{{EMAIL}}", LatexUtils.escapeLatex(data.getEmail()));
            latexContent = latexContent.replace("{{PHONE}}", LatexUtils.escapeLatex(data.getPhone()));
            latexContent = latexContent.replace("{{GITHUB}}", LatexUtils.escapeLatex(data.getGithub()));

            // 3. ĐỔ DỮ LIỆU VÀO CÁC DANH SÁCH THEO TỪNG THỂ LOẠI TEMPLATE
// ==========================================
            // 3. ĐỔ DỮ LIỆU VÀO CÁC DANH SÁCH THEO TỪNG THỂ LOẠI TEMPLATE
            // ==========================================

            // 3.1 Khối SKILLS
            if (hasSkills) {
                StringBuilder skillBuilder = new StringBuilder();
                if (isResumeTemplate) {
                    // Sửa: Dùng text phẳng phân cách bằng dấu phẩy thay vì itemize làm phình bảng
                    for (int i = 0; i < data.getSkills().size(); i++) {
                        skillBuilder.append(LatexUtils.escapeLatex(data.getSkills().get(i)));
                        if (i < data.getSkills().size() - 1) {
                            skillBuilder.append(", ");
                        }
                    }
                    skillBuilder.append("\n");
                } else {
                    for (String skill : data.getSkills()) {
                        skillBuilder.append("\\cvtag{").append(LatexUtils.escapeLatex(skill)).append("} \n");
                    }
                }
                latexContent = latexContent.replace("{{SKILLS_LIST}}", skillBuilder.toString());
            }

            // 3.2 Khối EXPERIENCES
            if (hasExp) {
                StringBuilder expBuilder = new StringBuilder();
                for (int i = 0; i < data.getExperiences().size(); i++) {
                    ExperienceDto exp = data.getExperiences().get(i);

                    if (isResumeTemplate) {
                        // Sửa: Dùng định dạng đoạn thẳng, tinh chỉnh khoảng cách bằng \vspace
                        expBuilder.append("\\noindent \\textbf{").append(LatexUtils.escapeLatex(exp.getCompany())).append("}")
                                .append(" \\hfill {").append(LatexUtils.escapeLatex(exp.getDuration())).append("}\\\\ \n")
                                .append("\\noindent \\textit{").append(LatexUtils.escapeLatex(exp.getRole())).append("} ");
                        if (exp.getLocation() != null && !exp.getLocation().isEmpty()) {
                            expBuilder.append(" -- \\textit{").append(LatexUtils.escapeLatex(exp.getLocation())).append("}");
                        }
                        expBuilder.append("\\\\[0.1cm] \n");

                        if (exp.getDescription() != null && !exp.getDescription().isEmpty()) {
                            for (String descItem : exp.getDescription()) {
                                if (descItem != null && !descItem.trim().isEmpty()) {
                                    expBuilder.append("$\\bullet$ ").append(LatexUtils.escapeLatex(descItem)).append("\\\\ \n");
                                }
                            }
                        }
                        expBuilder.append("\\vspace{0.2cm}\n");
                    } else {
                        expBuilder.append("\\cvevent{")
                                .append(LatexUtils.escapeLatex(exp.getRole())).append("}{")
                                .append(LatexUtils.escapeLatex(exp.getCompany())).append("}{")
                                .append(LatexUtils.escapeLatex(exp.getDuration())).append("}{")
                                .append(LatexUtils.escapeLatex(exp.getLocation())).append("}\n");

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
                    }
                    expBuilder.append("\n");
                }
                latexContent = latexContent.replace("{{EXPERIENCES_LIST}}", expBuilder.toString());
            }

            // 3.3 Khối PROJECTS
            if (hasProjects) {
                StringBuilder projBuilder = new StringBuilder();
                for (int i = 0; i < data.getProjects().size(); i++) {
                    ProjectDto proj = data.getProjects().get(i);

                    if (isResumeTemplate) {
                        // Sửa: Format phẳng tối giản giúp bảng tự co giãn dòng chính xác
                        projBuilder.append("\\noindent \\textbf{").append(LatexUtils.escapeLatex(proj.getName())).append("}")
                                .append(" \\hfill {").append(LatexUtils.escapeLatex(proj.getDuration())).append("}\\\\ \n")
                                .append("\\noindent \\textit{Vai trò: ").append(LatexUtils.escapeLatex(proj.getRole())).append("}\\\\[0.1cm] \n");

                        if (proj.getDescription() != null && !proj.getDescription().isEmpty()) {
                            for (String descItem : proj.getDescription()) {
                                if (descItem != null && !descItem.trim().isEmpty()) {
                                    projBuilder.append("$\\bullet$ ").append(LatexUtils.escapeLatex(descItem)).append("\\\\ \n");
                                }
                            }
                        }
                        projBuilder.append("\\vspace{0.2cm}\n");
                    } else {
                        projBuilder.append("\\cvevent{")
                                .append(LatexUtils.escapeLatex(proj.getRole())).append("}{")
                                .append(LatexUtils.escapeLatex(proj.getName())).append("}{")
                                .append(LatexUtils.escapeLatex(proj.getDuration())).append("}{}\n");

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
                    }
                    projBuilder.append("\n");
                }
                latexContent = latexContent.replace("{{PROJECTS_LIST}}", projBuilder.toString());
            }

            // 3.4 Khối EDUCATIONS
            if (hasEdu) {
                StringBuilder eduBuilder = new StringBuilder();
                for (int i = 0; i < data.getEducations().size(); i++) {
                    EducationDto edu = data.getEducations().get(i);

                    if (isResumeTemplate) {
                        eduBuilder.append("\\noindent \\textbf{").append(LatexUtils.escapeLatex(edu.getUniversity())).append("}")
                                .append(" \\hfill {").append(LatexUtils.escapeLatex(edu.getDuration())).append("}\\\\ \n")
                                .append("\\noindent \\textit{").append(LatexUtils.escapeLatex(edu.getDegree())).append("} \\\\ \n");

                        if (edu.getDetails() != null && !edu.getDetails().trim().isEmpty()) {
                            eduBuilder.append("\\noindent {\\small ").append(LatexUtils.escapeLatex(edu.getDetails())).append("} \\\\ \n");
                        }
                        eduBuilder.append("\\vspace{0.1cm}\n");
                    } else {
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
                    }
                    eduBuilder.append("\n");
                }
                latexContent = latexContent.replace("{{EDUCATIONS_LIST}}", eduBuilder.toString());
            }

            // 3.5 Khối LANGUAGES
            if (hasLanguages) {
                StringBuilder langBuilder = new StringBuilder();
                if (isResumeTemplate) {
                    for (LanguageDto lang : data.getLanguages()) {
                        langBuilder.append("  \\item \\textbf{").append(LatexUtils.escapeLatex(lang.getLanguage())).append("}: ")
                                .append(LatexUtils.escapeLatex(String.valueOf(lang.getLevel()))).append("\n");
                    }
                } else {
                    for (int i = 0; i < data.getLanguages().size(); i++) {
                        LanguageDto lang = data.getLanguages().get(i);
                        langBuilder.append("\\cvskill{")
                                .append(LatexUtils.escapeLatex(lang.getLanguage())).append("}{")
                                .append(lang.getLevel()).append("}\n");

                        if (i < data.getLanguages().size() - 1) {
                            langBuilder.append("\\divider\n");
                        }
                    }
                }
                latexContent = latexContent.replace("{{LANGUAGES_LIST}}", langBuilder.toString());
            }

            // 3.6 Khối ACHIEVEMENTS
            if (hasAchievements) {
                StringBuilder achBuilder = new StringBuilder();
                if (isResumeTemplate) {
                    for (String ach : data.getAchievements()) {
                        achBuilder.append("\\noindent $\\bullet$ ").append(LatexUtils.escapeLatex(ach)).append("\\\\ \n");
                    }
                } else {
                    for (int i = 0; i < data.getAchievements().size(); i++) {
                        String ach = data.getAchievements().get(i);
                        achBuilder.append("\\cvachievement{\\faTrophy}{")
                                .append(LatexUtils.escapeLatex(ach)).append("}{}\n");

                        if (i < data.getAchievements().size() - 1) {
                            achBuilder.append("\\divider\n");
                        }
                    }
                }
                latexContent = latexContent.replace("{{ACHIEVEMENTS_LIST}}", achBuilder.toString());
            }

            // 3.7 Khối REFEREES
            if (hasReferees) {
                StringBuilder refBuilder = new StringBuilder();
                if (isResumeTemplate) {
                    for (RefereeDto ref : data.getReferees()) {
                        refBuilder.append("\\noindent \\textbf{").append(LatexUtils.escapeLatex(ref.getName())).append("}")
                                .append(" -- ").append(LatexUtils.escapeLatex(ref.getCompany())).append("\\\\ \n")
                                .append("\\noindent Email: ").append(LatexUtils.escapeLatex(ref.getEmail()))
                                .append(" | SĐT: ").append(LatexUtils.escapeLatex(ref.getPhone())).append("\\\\ \n");
                    }
                } else {
                    for (int i = 0; i < data.getReferees().size(); i++) {
                        RefereeDto ref = data.getReferees().get(i);
                        refBuilder.append("\\cvref{")
                                .append(LatexUtils.escapeLatex(ref.getName())).append("}{")
                                .append(LatexUtils.escapeLatex(ref.getCompany())).append("}{")
                                .append(LatexUtils.escapeLatex(ref.getEmail())).append("}{\n")
                                .append(LatexUtils.escapeLatex(ref.getPhone())).append("}\n");

                        if (i < data.getReferees().size() - 1) {
                            refBuilder.append("\\divider\n");
                        }
                    }
                }
                latexContent = latexContent.replace("{{REFEREES_LIST}}", refBuilder.toString());
            }
            // 4. DỌN DẸP CUỐI CÙNG: Xóa toàn bộ các token thừa hoặc rỗng không có dữ liệu
            latexContent = latexContent
                    .replace("{{BEGIN_SUMMARY}}", "").replace("{{END_SUMMARY}}", "")
                    .replace("{{BEGIN_EDUCATIONS}}", "").replace("{{END_EDUCATIONS}}", "")
                    .replace("{{BEGIN_SKILLS}}", "").replace("{{END_SKILLS}}", "")
                    .replace("{{BEGIN_PROJECTS}}", "").replace("{{END_PROJECTS}}", "")
                    .replace("{{BEGIN_ROLES}}", "").replace("{{END_ROLES}}", "")
                    .replace("{{BEGIN_EXPERIENCES}}", "").replace("{{END_EXPERIENCES}}", "")
                    .replace("{{BEGIN_ACHIEVEMENTS}}", "").replace("{{END_ACHIEVEMENTS}}", "")
                    .replace("{{BEGIN_REFEREES}}", "").replace("{{END_REFEREES}}", "");

            latexContent = latexContent
                    .replace("{{SUMMARY_TEXT}}", "")
                    .replace("{{EDUCATIONS_LIST}}", "")
                    .replace("{{SKILLS_LIST}}", "")
                    .replace("{{PROJECTS_LIST}}", "")
                    .replace("{{ROLES_LIST}}", "")
                    .replace("{{EXPERIENCES_LIST}}", "")
                    .replace("{{ACHIEVEMENTS_LIST}}", "")
                    .replace("{{REFEREES_LIST}}", "")
                    .replace("{{TAGLINE}}", "")
                    .replace("{{EMAIL}}", "")
                    .replace("{{PHONE}}", "")
                    .replace("{{GITHUB}}", "");

            // Ghi file tex ra thư mục tạm của session
            Files.writeString(texFilePath, latexContent, StandardCharsets.UTF_8);

            // --- 5. Thực thi tiến trình biên dịch XeLaTeX ---
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "xelatex",
                    "-interaction=nonstopmode",
                    "-halt-on-error",
                    texFileName
            );
            processBuilder.directory(jobDir.toFile());
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            StringBuilder outputLog = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    outputLog.append(line).append("\n");
                }
            }

            boolean finished = process.waitFor(120, TimeUnit.SECONDS);
            if (!finished) {
                process.destroy();
                throw new RuntimeException("Quá thời gian biên dịch PDF");
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                System.err.println("=== LỖI BIÊN DỊCH LATEX (Exit Code: " + exitCode + ") ===");
                System.err.println(outputLog.toString());
                throw new RuntimeException("Lỗi cú pháp LaTeX. Hãy kiểm tra console log!");
            }

            if (Files.exists(pdfFilePath)) {
                System.out.println("Đã sinh file PDF tại: " + pdfFilePath);
                return Files.readAllBytes(pdfFilePath);
            } else {
                System.err.println("=== LỖI BIÊN DỊCH LATEX ===");
                System.err.println(outputLog.toString());
                throw new RuntimeException("Không tìm thấy file PDF đầu ra.");
            }

        } finally {
            try (java.util.stream.Stream<Path> walk = Files.walk(jobDir)) {
                walk.sorted(java.util.Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException e) {
                                System.err.println("Không thể xóa file tạm: " + path + " - " + e.getMessage());
                            }
                        });
            } catch (IOException e) {
                System.err.println("Không thể dọn dẹp thư mục tạm thời: " + e.getMessage());
            }
        }
    }
    private String processBlock(String latexContent, String blockName, boolean keep) {
        String regex = "\\{\\{BEGIN_" + blockName + "\\}\\}(.*?)\\{\\{END_" + blockName + "\\}\\}";
        if (keep) {
            return latexContent.replaceAll("(?s)" + regex, "$1");
        } else {
            return latexContent.replaceAll("(?s)" + regex, "");
        }
    }

}