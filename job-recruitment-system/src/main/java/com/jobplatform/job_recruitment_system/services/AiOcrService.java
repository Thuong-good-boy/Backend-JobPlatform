package com.jobplatform.job_recruitment_system.services;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jobplatform.job_recruitment_system.dtos.Response.AiBreakdownResultResponse;
import com.jobplatform.job_recruitment_system.dtos.Response.MatchResultReponse;
import com.jobplatform.job_recruitment_system.dtos.Response.OcrResultReponse;
import com.jobplatform.job_recruitment_system.dtos.Response.SkillValidationResponse;
import com.jobplatform.job_recruitment_system.dtos.request.AutoFixRequest;
import com.jobplatform.job_recruitment_system.dtos.request.CvRequest;
import com.jobplatform.job_recruitment_system.models.Cv;
import com.jobplatform.job_recruitment_system.models.Job;
import com.jobplatform.job_recruitment_system.repositories.CvRepository;
import com.jobplatform.job_recruitment_system.repositories.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiOcrService {

    @Value("${gemini.api-key}")
    private List<String> apiKeys;
    private final java.util.concurrent.atomic.AtomicInteger currentKeyIndex = new java.util.concurrent.atomic.AtomicInteger(0);
    private final SkillRepository skillRepository;
    private final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private  final CvRepository repository;

    private RestTemplate getSecureRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000);
        factory.setReadTimeout(35000);
        return new RestTemplate(factory);
    }

    private HttpHeaders createGoogleHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
    private HttpHeaders createOpenRouterHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKeys);
        headers.set("HTTP-Referer", "https://pathuongdev.id.vn");
        headers.set("X-Title", "Job Recruitment System");
        return headers;
    }

    public OcrResultReponse extractCompanyInfo(MultipartFile file) throws IOException {
        System.out.println("có vào ai : ");
        String base64ImageRaw = Base64.getEncoder().encodeToString(file.getBytes());
        String prompt = "Bạn là hệ thống OCR đọc giấy chứng nhận đăng ký doanh nghiệp Việt Nam.\n" +
                "Nhiệm vụ: chỉ trích xuất MÃ SỐ THUẾ và WEBSITE từ ảnh.\n\n" +
                "1. 'taxCode':\n" +
                "- Tìm dãy số cạnh các nhãn:\n" +
                "  + 'Mã số doanh nghiệp'\n" +
                "  + 'Mã số thuế'\n" +
                "- Chỉ lấy phần số.\n\n" +
                "2. 'website':\n" +
                "- Chỉ lấy WEBSITE của doanh nghiệp.\n" +
                "- Ví dụ hợp lệ:\n" +
                "  + abc.com.vn\n" +
                "  + www.abc.vn\n" +
                "- KHÔNG lấy email.\n" +
                "- Nếu là email như abc@gmail.com thì bỏ qua.\n" +
                "- Nếu không có website thì trả về null.\n\n" +
                "Yêu cầu bắt buộc:\n" +
                "- Chỉ trả về DUY NHẤT 1 JSON hợp lệ.\n" +
                "- Không giải thích.\n" +
                "- Format:\n" +
                "{\"taxCode\":\"...\",\"website\":\"...\"}\n" +
                "- Nếu không tìm thấy thì trả về null.";

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt),

                                Map.of("inlineData", Map.of(
                                        "mimeType", file.getContentType(),
                                        "data", base64ImageRaw
                                ))
                        ))
                )
        );

        return callGeminiAndParseJson("gemini-2.5-flash-lite", requestBody, OcrResultReponse.class);
    }

    public String extractCvInfoToJson(MultipartFile file) throws IOException {
        String base64ImageRaw = "";

        String contentType = file.getContentType();
        if (contentType != null && contentType.equalsIgnoreCase("application/pdf")) {
            try (PDDocument document = PDDocument.load(file.getInputStream())) {
                PDFRenderer pdfRenderer = new PDFRenderer(document);
                BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 150);

                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    ImageIO.write(bim, "png", baos);
                    byte[] imageBytes = baos.toByteArray();
                    base64ImageRaw = Base64.getEncoder().encodeToString(imageBytes);
                }
            }
        } else {
            base64ImageRaw = Base64.getEncoder().encodeToString(file.getBytes());
        }

        List<String> systemSkills = skillRepository.getALlSkillName();
        String skillListStr = String.join(", ", systemSkills);

        String prompt = "Bạn là chuyên gia nhân sự và AI trích xuất dữ liệu. Hãy đọc CV đính kèm và trích xuất thông tin ứng viên vào một file JSON.\n\n" +
                "QUY TẮC TỐI THƯỢNG BẮT BUỘC:\n" +
                "1. BẠN PHẢI TRẢ VỀ ĐẦY ĐỦ 100% CÁC KEY TRONG CẤU TRÚC BÊN DƯỚI. Tuyệt đối không được bỏ sót bất kỳ key nào.\n" +
                "2. Nếu CV KHÔNG CÓ thông tin cho một trường nào đó, bắt buộc để chuỗi rỗng \"\" (với text), số 0 hoặc 0.0 (với số), hoặc mảng rỗng [] (với mảng). KHÔNG được tự bịa dữ liệu.\n" +
                "3. Ở 'description' của 'experiences' và 'projects', tách các ý thành các phần tử chuỗi trong mảng.\n" +
                "4. Đối với trường 'icon' trong 'achievements' và 'publications', hãy tự đề xuất các mã icon FontAwesome phù hợp (Ví dụ: \"\\\\faTrophy\", \"\\\\faBook\", \"\\\\faStar\").\n" +
                "5. Đối với 'dayOfLife', hãy ước lượng thời gian cho 'hours', giữ nguyên các giá trị mặc định cho 'textWidth' (ví dụ: \"6em\") và 'color' (ví dụ: \"accent\", \"emphasis\", \"body\") nếu không có yêu cầu cụ thể.\n\n" +
                "CẤU TRÚC JSON ÉP BUỘC PHẢI TUÂN THEO:\n" +
                "{\n" +
                "  \"fullName\": \"\",\n" +
                "  \"jobTitle\": \"\",\n" +
                "  \"avatarUrl\": \"\",\n" +
                "  \"phone\": \"\",\n" +
                "  \"email\": \"\",\n" +
                "  \"address\": \"\",\n" +
                "  \"github\": \"\",\n" +
                "  \"linkedin\": \"\",\n" +
                "  \"homepage\": \"\",\n" +
                "  \"twitter\": \"\",\n" +
                "  \"gitlab\": \"\",\n" +
                "  \"orcid\": \"\",\n" +
                "  \"philosophy\": \"\",\n" +
                "  \"templateName\": \"\",\n" +
                "  \"skills\": [],\n" +
                "  \"experiences\": [\n" +
                "    {\n" +
                "      \"role\": \"\",\n" +
                "      \"company\": \"\",\n" +
                "      \"duration\": \"\",\n" +
                "      \"location\": \"\",\n" +
                "      \"description\": []\n" +
                "    }\n" +
                "  ],\n" +
                "  \"projects\": [\n" +
                "    {\n" +
                "      \"name\": \"\",\n" +
                "      \"role\": \"\",\n" +
                "      \"duration\": \"\",\n" +
                "      \"description\": []\n" +
                "    }\n" +
                "  ],\n" +
                "  \"educations\": [\n" +
                "    {\n" +
                "      \"degree\": \"\",\n" +
                "      \"university\": \"\",\n" +
                "      \"duration\": \"\",\n" +
                "      \"details\": \"\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"languages\": [\n" +
                "    {\n" +
                "      \"language\": \"\",\n" +
                "      \"level\": 0.0\n" +
                "    }\n" +
                "  ],\n" +
                "  \"achievements\": [\n" +
                "    {\n" +
                "      \"icon\": \"\",\n" +
                "      \"title\": \"\",\n" +
                "      \"details\": \"\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"dayOfLife\": [\n" +
                "    {\n" +
                "      \"hours\": 0.0,\n" +
                "      \"textWidth\": \"6em\",\n" +
                "      \"color\": \"accent\",\n" +
                "      \"text\": \"\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"publications\": [\n" +
                "    {\n" +
                "      \"icon\": \"\",\n" +
                "      \"title\": \"\",\n" +
                "      \"authors\": \"\",\n" +
                "      \"year\": \"\",\n" +
                "      \"publisher\": \"\"\n" +
                "    }\n" +
                "  ]\n" +
                "}\n\n" +
                "YÊU CẦU QUAN TRỌNG VỀ SKILLS: Mảng 'skills' CHỈ ĐƯỢC PHÉP chứa các từ khóa nằm trong danh sách chuẩn sau: [" + skillListStr + "]. Nếu CV có kỹ năng không khớp, hãy bỏ qua.\n" +
                "ĐẦU RA: Chỉ trả về chuỗi JSON hợp lệ, không bọc bằng thẻ markdown (như ```json), không in ra bất kỳ đoạn text giải thích nào khác.";

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt),
                                Map.of("inlineData", Map.of(
                                        "mimeType", "image/png",
                                        "data", base64ImageRaw.replaceAll("[\\s\\r\\n]", "")
                                ))
                        ))
                )
        );
        return executeDirectGeminiRequest("gemini-2.5-flash-lite", requestBody);
    }

    public MatchResultReponse calculateMatchScore(String cvJsonData, Job job) {
        String prompt = """
                Bạn là một chuyên gia tuyển dụng (HR) cấp cao.
                
                Nhiệm vụ của bạn là đánh giá mức độ phù hợp giữa CV ứng viên và Yêu cầu công việc (JD).
                
                --- THÔNG TIN CV (JSON) ---
                %s
                
                --- YÊU CẦU CÔNG VIỆC (JD) ---
                %s
                
                QUY TẮC ĐÁNH GIÁ:
                
                1. skillScore (0.0 - 100.0)
                - Phân tích các kỹ năng được yêu cầu trong JD.
                - So sánh với kỹ năng trong CV.
                - Nếu ứng viên đáp ứng đầy đủ các kỹ năng quan trọng của JD thì skillScore = 100.
                - Nếu thiếu kỹ năng quan trọng thì giảm điểm tương ứng.
                - Chỉ đánh giá dựa trên mức độ phù hợp với JD hiện tại.
                
                2. experienceScore (0.0 - 100.0)
                - Phân tích toàn bộ kinh nghiệm và dự án trong CV.
                - Xác định vị trí đang tuyển từ JD.
                - Đánh giá mức độ liên quan giữa các dự án của ứng viên với vị trí đó.
                - Đánh giá vai trò của ứng viên trong từng dự án (role).
                - Vai trò càng gần với vị trí đang tuyển thì điểm càng cao.
                - Trách nhiệm, mức độ đóng góp và công nghệ sử dụng phải được xem xét.
                - Không chỉ dựa trên số năm kinh nghiệm.
                
                3. educationScore (0.0 - 100.0)
                - Đánh giá mức độ liên quan giữa ngành học và vị trí tuyển dụng.
                - Đánh giá trình độ học vấn.
                - Xem xét uy tín/chất lượng trường học nếu có thông tin.
                - Xem xét GPA, xếp loại tốt nghiệp, học bổng, giải thưởng, chứng chỉ và các thành tích liên quan.
                - Chỉ cộng điểm khi các yếu tố này thực sự hỗ trợ cho vị trí tuyển dụng.
                
                4. overallScore (0.0 - 100.0)
                - Tự động xác định tầm quan trọng của kỹ năng, kinh nghiệm và học vấn dựa trên JD.
                - Không sử dụng trọng số cố định.
                - Với mỗi vị trí tuyển dụng, hãy tự suy luận yếu tố nào quan trọng hơn.
                - overallScore phải phản ánh mức độ phù hợp tổng thể với JD.
                
                5. reason
                - Giải thích ngắn gọn dưới 30 từ.
                - Nêu rõ điểm mạnh và điểm còn thiếu quan trọng nhất.
                
                YÊU CẦU:
                - Chỉ trả về duy nhất JSON hợp lệ.
                - Không markdown.
                - Không giải thích ngoài JSON.
                
                Định dạng:
                
                {
                  "skillScore": 0.0,
                  "experienceScore": 0.0,
                  "educationScore": 0.0,
                  "overallScore": 0.0,
                  "reason": ""
                }
                """.formatted(cvJsonData, job.getDescription());

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                ),
                "generationConfig", Map.of(
                        "temperature", 0.1
                )
        );

        AiBreakdownResultResponse breakdown = callGeminiAndParseJson("gemini-2.5-flash-lite", requestBody, AiBreakdownResultResponse.class);

        if (breakdown == null) {
            MatchResultReponse fallback = new MatchResultReponse();
            fallback.setScore(0.0);
            fallback.setReason("Hệ thống AI phân tích đang bận, vui lòng thử lại sau.");
            return fallback;
        }

        double weightSkill = job.getWeightSkill();
        double weightExperience = job.getWeightExperience();
        double weightEducation = job.getWeightEducation();

        double finalScore = (breakdown.getSkillScore() * weightSkill) +
                (breakdown.getExperienceScore() * weightExperience) +
                (breakdown.getEducationScore() * weightEducation);

        finalScore = Math.round(finalScore * 10.0) / 10.0;

        MatchResultReponse finalResponse = new MatchResultReponse();
        finalResponse.setScore(finalScore);
        finalResponse.setReason(breakdown.getReason());

        return finalResponse;
    }
    public String evaluateCvFromUrl(String fileUrl) throws Exception {
        List<Map<String, Object>> partsList = new ArrayList<>();

        String prompt = "Bạn là một Headhunter cấp cao kiêm Chuyên gia Copywriter. Hãy đọc thật kỹ nội dung trong (các) hình ảnh CV này.\n" +
                "Nhiệm vụ của bạn KHÔNG PHẢI là đánh giá thiết kế, mà là TỐI ƯU HÓA NỘI DUNG. Hãy:\n" +
                "1. Soi và nhặt ra các lỗi chính tả, lỗi gõ phím, lỗi ngữ pháp tiếng Việt/tiếng Anh.\n" +
                "2. Tìm các câu văn lủng củng, diễn đạt yếu hoặc mô tả kinh nghiệm chưa đủ 'chạm' và viết lại chúng sao cho chuyên nghiệp, ấn tượng và mang ngôn ngữ của người đạt thành tựu (hướng kết quả).\n" +
                "Trả về kết quả dưới dạng JSON có cấu trúc sau:\n" +
                "{\n" +
                "  \"spelling_and_grammar\": [\n" +
                "    {\"error\": \"[Trích dẫn từ viết sai/lỗi]\", \"fix\": \"[Từ/câu sửa lại cho đúng]\"}\n" +
                "  ],\n" +
                "  \"better_phrasing\": [\n" +
                "    {\"original\": \"[Câu gốc lủng củng/yếu trong CV]\", \"suggestion\": \"[Câu viết lại sắc bén, chuyên nghiệp hơn]\"}\n" +
                "  ],\n" +
                "  \"general_advice\": [\"[Lời khuyên 1 về cách hành văn/nội dung]\", \"[Lời khuyên 2]\"]\n" +
                "}\n" +
                "YÊU CẦU QUAN TRỌNG: Chỉ trả về ĐÚNG chuỗi JSON hợp lệ, không bọc trong thẻ Markdown (như ```json), không giải thích thêm bất kỳ câu nào bên ngoài JSON.";

        partsList.add(Map.of("text", prompt));

        URL url = new URL(fileUrl);
        try (InputStream in = url.openStream()) {
            if (fileUrl.toLowerCase().endsWith(".pdf") || fileUrl.contains("cloudinary.com")) {
                try (PDDocument document = PDDocument.load(in)) {
                    PDFRenderer pdfRenderer = new PDFRenderer(document);

                    int pageCount = Math.min(document.getNumberOfPages(), 3);

                    for (int page = 0; page < pageCount; page++) {
                        BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 150);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(bim, "jpeg", baos);

                        String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());

                        partsList.add(Map.of("inlineData", Map.of(
                                "mimeType", "image/jpeg",
                                "data", base64Image.replaceAll("[\\s\\r\\n]", "")
                        )));
                    }
                }
            } else {
                byte[] fileBytes = in.readAllBytes();
                String mimeType = fileUrl.toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
                String base64Image = Base64.getEncoder().encodeToString(fileBytes);

                partsList.add(Map.of("inlineData", Map.of(
                        "mimeType", mimeType,
                        "data", base64Image.replaceAll("[\\s\\r\\n]", "")
                )));
            }
        }

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", partsList)
                )
        );

        return executeDirectGeminiRequest("gemini-2.5-flash-lite", requestBody);
    }
    public CvRequest rewriteCvData(AutoFixRequest request) {

        Cv cv = repository.findById(request.getCvId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy CV với ID: " + request.getCvId()));

        ObjectMapper mapper = new ObjectMapper();
        String jsonInput = "";
        String jsonFeedback = "";

        try {
            if (cv.getCvData() instanceof String) {
                jsonInput = (String) cv.getCvData();
            } else {
                jsonInput = mapper.writeValueAsString(cv.getCvData());
            }

            if (request.getFeedback() != null) {
                jsonFeedback = mapper.writeValueAsString(request.getFeedback());
            } else {
                jsonFeedback = "{}";
            }

            String prompt = "Bạn là một Headhunter cấp cao kiêm Copywriter chuyên nghiệp.\n\n" +
                    "Dưới đây là dữ liệu CV gốc của ứng viên (định dạng JSON):\n" +
                    jsonInput + "\n\n" +
                    "Dưới đây là danh sách các lỗi chính tả, lỗi ngữ pháp và các gợi ý nâng cấp văn phong cần áp dụng (định dạng JSON):\n" +
                    jsonFeedback + "\n\n" +
                    "Nhiệm vụ của bạn:\n" +
                    "1. Hãy đọc kỹ danh sách gợi ý trong 'better_phrasing' và sửa đổi/thay thế chính xác các câu tương ứng có trong CV gốc.\n" +
                    "2. Kiểm tra và khắc phục thêm các lỗi trong 'spelling_and_grammar' (nếu có) vào các trường dữ liệu tương ứng.\n" +
                    "3. Nâng cấp văn phong tại các field: 'summary', 'description' dựa trên các gợi ý đó sao cho chuyên nghiệp, sắc bén.\n" +
                    "4. TUYỆT ĐỐI KHÔNG thay đổi cấu trúc JSON ban đầu của CV (keys, mảng, object).\n" +
                    "5. TUYỆT ĐỐI KHÔNG thay đổi dữ liệu của các field định danh: fullName, email, phone, avatarUrl, address, github, linkedin, templateName, ngày tháng.\n\n" +
                    "YÊU CẦU TỐI THƯỢNG: Chỉ trả về duy nhất chuỗi JSON hợp lệ của CV đã được nâng cấp hoàn chỉnh theo đúng cấu trúc cũ. Không bọc trong thẻ Markdown (như ```json), không giải thích thêm.";

            // 1. Chuyển đổi cấu trúc Body sang chuẩn yêu cầu của Google Gemini API
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt)
                            ))
                    ),
                    "generationConfig", Map.of(
                            "temperature", 0.2
                    )
            );

            CvRequest aiResponse = callGeminiAndParseJson("gemini-2.5-flash-lite", requestBody, CvRequest.class);

            // 3. Nếu AI trả về null (do lỗi key hoặc parse thất bại), kích hoạt luồng fallback về data gốc
            if (aiResponse == null) {
                System.err.println("AI xử lý thất bại hoặc lỗi kết nối, fallback về data gốc.");
                return mapper.readValue(jsonInput, CvRequest.class);
            }

            return aiResponse;

        } catch (Exception e) {
            System.err.println("Lỗi trong quá trình AI xử lý áp dụng Feedback cho CV: " + e.getMessage());
            System.err.println("Fallback: Sử dụng lại dữ liệu CV gốc để tiếp tục tạo PDF.");

            try {
                return mapper.readValue(jsonInput, CvRequest.class);
            } catch (Exception ex) {
                System.err.println("Không thể parse dữ liệu gốc, trả về object rỗng.");
                return new CvRequest();
            }
        }
    }
    public SkillValidationResponse validateAndNormalizeNewSkill(String rawSkillName) {
        String prompt = "Bạn là một chuyên gia Hệ thống dữ liệu Nhân sự IT.\n" +
                "Nhiệm vụ của bạn là kiểm tra một từ khóa do ứng viên nhập vào xem có phải là một Kỹ năng chuyên môn, Công cụ, hoặc Công nghệ hợp lệ để đưa vào CV hay không.\n\n" +
                "Từ khóa cần kiểm tra: \"" + rawSkillName + "\"\n\n" +
                "QUY TẮC XỬ LÝ:\n" +
                "1. Nếu từ khóa là một kỹ năng/công nghệ/công cụ có thật (dù mới xuất hiện), hãy xác định 'valid' = true.\n" +
                "2. Hãy CHUẨN HÓA lại định dạng chữ viết hoa/viết thường theo đúng chuẩn technical quốc tế tại trường 'standardizedName'. Ví dụ:\n" +
                "   - 'reactjs' hoặc 'react js' -> 'React'\n" +
                "   - 'nodejs' -> 'Node.js'\n" +
                "   - 'vue' -> 'Vue.js'\n" +
                "   - 'aws' -> 'AWS'\n" +
                "   - 'docker' -> 'Docker'\n" +
                "3. Nếu từ khóa là từ vô nghĩa, câu chửi, từ lăng mạ, hoặc không liên quan gì đến kỹ năng làm việc (Ví dụ: 'ahihi', 'đẹp trai', 'ăn cơm'), hãy trả về 'valid' = false và điền lý do vào trường 'reason'.\n\n" +
                "YÊU CẦU ĐẦU RA: Chỉ trả về duy nhất chuỗi JSON hợp lệ theo định dạng sau, không kèm markdown, không giải thích dông dài:\n" +
                "{\n" +
                "  \"valid\": true,\n" +
                "  \"standardizedName\": \"\",\n" +
                "  \"reason\": \"\"\n" +
                "}";

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                ),
                "generationConfig", Map.of(
                        "temperature", 0.0 // Giữ chặt độ chính xác, không cho AI sáng tạo lung tung
                )
        );

        SkillValidationResponse response = callGeminiAndParseJson("gemini-2.5-flash-lite", requestBody, SkillValidationResponse.class);

        if (response == null) {
            SkillValidationResponse fallback = new SkillValidationResponse();
            fallback.setValid(false);
            fallback.setReason("Hệ thống kiểm tra đang bận.");
            return fallback;
        }

        return response;
    }

    private String executeDirectGeminiRequest(String modelName, Map<String, Object> body) {
        RestTemplate restTemplate = getSecureRestTemplate();
        int maxRetries = apiKeys.size();
        Exception lastException = null;

        for (int i = 0; i < maxRetries; i++) {
            int index = Math.abs(currentKeyIndex.getAndIncrement() % apiKeys.size());
            String activeKey = apiKeys.get(index).trim();

            String finalUrl = GEMINI_BASE_URL + modelName + ":generateContent?key=" + activeKey;

            System.out.println(">>> [AI OCR] Thử lần " + (i + 1) + "/" + maxRetries + " - Dùng Key index [" + index + "]");

            try {
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, createGoogleHeaders());
                 Map<String, Object> response = restTemplate.postForObject(finalUrl, entity, Map.class);
                return extractJsonFromGoogleResponse(response);

            } catch (org.springframework.web.client.HttpStatusCodeException e) {
                lastException = e;
                System.err.println(">>> [AI OCR] Key index [" + index + "] lỗi HTTP " + e.getStatusCode() + ": " + e.getResponseBodyAsString());

            } catch (Exception e) {
                lastException = e;
                System.err.println(">>> [AI OCR] Key index [" + index + "] lỗi hệ thống: " + e.getMessage());
            }
        }

        System.err.println(">>> [AI OCR] TẤT CẢ CÁC KEY ĐỀU THẤT BẠI!");
        if (lastException != null) {
            throw new RuntimeException("Tất cả API Key đều không khả dụng. Lỗi cuối cùng: " + lastException.getMessage(), lastException);
        }
        return "{}";
    }
    private String extractJsonFromGoogleResponse(Map<String, Object> response) {
        try {
            if (response != null && response.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> contentMap = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) contentMap.get("parts");
                    if (!parts.isEmpty()) {
                        String textResult = (String) parts.get(0).get("text");
                        System.out.println(">>> Google Gemini Raw: " + textResult);

                        Pattern pattern = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
                        Matcher matcher = pattern.matcher(textResult);
                        if (matcher.find()) {
                            return matcher.group();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi bóc tách cấu trúc Google Response: " + e.getMessage());
        }
        return "{}";
    }
    private <T> T callGeminiAndParseJson(String modelName, Map<String, Object> body, Class<T> clazz) {
        try {
            String jsonStr = executeDirectGeminiRequest(modelName, body);
            return new tools.jackson.databind.ObjectMapper().readValue(jsonStr, clazz);
        } catch (Exception e) {
            System.err.println(">>> LỖI GỌI GEMINI HOẶC PARSE JSON CHÍNH XÁC LÀ: ");
            e.printStackTrace();
            return null;
        }
    }



}