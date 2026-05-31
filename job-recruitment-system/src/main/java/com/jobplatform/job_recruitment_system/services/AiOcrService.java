package com.jobplatform.job_recruitment_system.services;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jobplatform.job_recruitment_system.dtos.Response.AiBreakdownResultResponse;
import com.jobplatform.job_recruitment_system.dtos.Response.MatchResultReponse;
import com.jobplatform.job_recruitment_system.dtos.Response.OcrResultReponse;
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
import org.springframework.stereotype.Service;
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
    private String apiKey;
    private final SkillRepository skillRepository;
    private final String OPEN_ROUTER_URL = "https://openrouter.ai/api/v1/chat/completions";
    private  final CvRepository repository;
    private HttpHeaders createOpenRouterHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("HTTP-Referer", "https://pathuongdev.id.vn");
        headers.set("X-Title", "Job Recruitment System");
        return headers;
    }

    public OcrResultReponse extractCompanyInfo(MultipartFile file) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        tools.jackson.databind.ObjectMapper mapper = new tools.jackson.databind.ObjectMapper();

        String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
        String dataUrl = "data:" + file.getContentType() + ";base64," + base64Image;

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
                "model", "google/gemini-2.0-flash-001",
                "messages", List.of(
                        Map.of("role", "user", "content", List.of(
                                Map.of("type", "text", "text", prompt),
                                Map.of("type", "image_url", "image_url", Map.of("url", dataUrl))
                        ))
                )
        );

        return callAiAndParseJson(requestBody, OcrResultReponse.class);
    }

    public String extractCvInfoToJson(MultipartFile file) throws IOException {
        String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
        String dataUrl = "data:" + file.getContentType() + ";base64," + base64Image;
        List<String> systemSkills = skillRepository.getALlSkillName();
        String skillListStr = String.join(", ", systemSkills);

        String prompt = "Bạn là chuyên gia nhân sự và AI trích xuất dữ liệu. Hãy đọc CV đính kèm và trích xuất thông tin ứng viên vào một file JSON.\n\n" +
                "QUY TẮC TỐI THƯỢNG BẮT BUỘC:\n" +
                "1. BẠN PHẢI TRẢ VỀ ĐẦY ĐỦ 100% CÁC KEY TRONG CẤU TRÚC BÊN DƯỚI. Tuyệt đối không được bỏ sót bất kỳ key nào.\n" +
                "2. Nếu CV KHÔNG CÓ thông tin, bắt buộc để chuỗi rỗng \"\" (với text) hoặc mảng rỗng [] (với mảng). KHÔNG được tự bịa dữ liệu.\n" +
                "3. Ở 'description' của 'experiences' và 'projects', tách các ý thành các phần tử chuỗi trong mảng.\n\n" +
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
                "  \"summary\": \"\",\n" +
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
                "      \"level\": 0\n" +
                "    }\n" +
                "  ],\n" +
                "  \"achievements\": [],\n" +
                "  \"referees\": [\n" +
                "    {\n" +
                "      \"name\": \"\",\n" +
                "      \"company\": \"\",\n" +
                "      \"email\": \"\",\n" +
                "      \"phone\": \"\"\n" +
                "    }\n" +
                "  ]\n" +
                "}\n\n" +
                "YÊU CẦU QUAN TRỌNG VỀ SKILLS: Mảng 'skills' CHỈ ĐƯỢC PHÉP chứa các từ khóa nằm trong danh sách chuẩn sau: [" + skillListStr + "]. Nếu CV có kỹ năng không khớp, hãy bỏ qua.\n" +
                "ĐẦU RA: Chỉ trả về chuỗi JSON hợp lệ, không bọc bằng thẻ markdown (như ```json), không in ra bất kỳ đoạn text giải thích nào khác.";
        Map<String, Object> requestBody = Map.of(
                "model", "google/gemini-2.0-flash-001",
                "messages", List.of(
                        Map.of("role", "user", "content", List.of(
                                Map.of("type", "text", "text", prompt),
                                Map.of("type", "image_url", "image_url", Map.of("url", dataUrl))
                        ))
                )
        );

        Map<String, Object> response = sendRequestToOpenRouter(requestBody);
        return extractJsonFromResponse(response);
    }

    public MatchResultReponse calculateMatchScore(String cvJsonData, Job job) {
        String prompt = "Bạn là một chuyên gia nhân sự (HR). Hãy phân tích mức độ phù hợp giữa CV và Yêu cầu công việc (JD).\n\n" +
                "--- THÔNG TIN CV (JSON) ---\n" + cvJsonData + "\n\n" +
                "--- YÊU CẦU CÔNG VIỆC ---\n" + job.getDescription() + "\n\n" +
                "Yêu cầu đánh giá chi tiết theo tiêu chí:\n" +
                "1. skillScore: Chấm từ 0.0 đến 100.0 dựa trên mức độ trùng khớp của kỹ năng cứng/mềm.\n" +
                "2. experienceScore: Chấm từ 0.0 đến 100.0 dựa trên số năm kinh nghiệm và vị trí tương đương.\n" +
                "3. educationScore: Chấm từ 0.0 đến 100.0 dựa trên bằng cấp, ngành học có đúng yêu cầu không.\n" +
                "4. reason: Đưa ra nhận xét ngắn gọn dưới 30 chữ.\n" +
                "Trả về DUY NHẤT 1 chuỗi JSON với cấu trúc: " +
                "{\"skillScore\": 90.0, \"experienceScore\": 80.0, \"educationScore\": 70.0, \"reason\": \"...\"}";

        Map<String, Object> requestBody = Map.of(
                "model", "openai/gpt-oss-120b",
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", 0.1
        );


        AiBreakdownResultResponse breakdown = callAiAndParseJson(requestBody, AiBreakdownResultResponse.class);

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
        List<Map<String, Object>> contentList = new ArrayList<>();
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

        contentList.add(Map.of("type", "text", "text", prompt));

        URL url = new URL(fileUrl);
        try (InputStream in = url.openStream()) {
            if (fileUrl.toLowerCase().endsWith(".pdf") || fileUrl.contains("[cloudinary.com/](https://cloudinary.com/)")) {
                // Xử lý file PDF (Dùng PDFBox để cắt từng trang thành ảnh)
                try (PDDocument document = PDDocument.load(in)) {
                    PDFRenderer pdfRenderer = new PDFRenderer(document);

                    // Chỉ lấy tối đa 3 trang đầu để tiết kiệm token
                    int pageCount = Math.min(document.getNumberOfPages(), 3);

                    for (int page = 0; page < pageCount; page++) {
                        // Render với độ phân giải 150 DPI là đủ nét cho AI đọc
                        BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 150);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(bim, "jpeg", baos);

                        String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());
                        String dataUrl = "data:image/jpeg;base64," + base64Image;

                        // Thêm ảnh của trang này vào list gửi cho AI
                        contentList.add(Map.of("type", "image_url", "image_url", Map.of("url", dataUrl)));
                    }
                }
            } else {
                byte[] fileBytes = in.readAllBytes();
                String mimeType = fileUrl.toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
                String base64Image = Base64.getEncoder().encodeToString(fileBytes);
                String dataUrl = "data:" + mimeType + ";base64," + base64Image;

                contentList.add(Map.of("type", "image_url", "image_url", Map.of("url", dataUrl)));
            }
        }

        // 3. Build Request Body
        Map<String, Object> requestBody = Map.of(
                "model", "google/gemini-2.0-flash-001",
                "messages", List.of(
                        Map.of("role", "user", "content", contentList)
                )
        );

        Map<String, Object> response = sendRequestToOpenRouter(requestBody);
        return extractJsonFromResponse(response);
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
                jsonFeedback = "{}"; // Nếu không có feedback thì để trống
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

            Map<String, Object> requestBody = Map.of(
                    "model", "google/gemini-2.0-flash-001",
                    "messages", List.of(
                            Map.of("role", "user", "content", prompt)
                    ),
                    "temperature", 0.2
            );

            Map<String, Object> response = sendRequestToOpenRouter(requestBody);

            String aiResponseJson = extractJsonFromResponse(response);

            if (aiResponseJson == null || !aiResponseJson.trim().startsWith("{")) {
                System.err.println("AI trả về sai format, fallback về data gốc.");
                return mapper.readValue(jsonInput, CvRequest.class);
            }

            return mapper.readValue(aiResponseJson, CvRequest.class);

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
    private Map<String, Object> sendRequestToOpenRouter(Map<String, Object> body) {
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, createOpenRouterHeaders());
        return restTemplate.postForObject(OPEN_ROUTER_URL, entity, Map.class);
    }

    private String extractJsonFromResponse(Map<String, Object> response) {
        if (response != null && response.containsKey("choices")) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (!choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                String content = (String) message.get("content");

                System.out.println(">>> AI Raw Result: " + content);

                Pattern pattern = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
                Matcher matcher = pattern.matcher(content);
                if (matcher.find()) {
                    return matcher.group();
                }
            }
        }
        return "{}";
    }

    private <T> T callAiAndParseJson(Map<String, Object> body, Class<T> clazz) {
        try {
            String jsonStr = extractJsonFromResponse(sendRequestToOpenRouter(body));
            return new tools.jackson.databind.ObjectMapper().readValue(jsonStr, clazz);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(">>> Lỗi AI: " + e.getMessage());
            return null;
        }

    }



}