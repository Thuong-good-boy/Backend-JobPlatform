package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.dtos.Response.MatchResultReponse;
import com.jobplatform.job_recruitment_system.dtos.Response.OcrResultReponse;
import com.jobplatform.job_recruitment_system.repositories.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AiOcrService {

    @Value("${gemini.api-key}")
    private String apiKey;
    private SkillRepository skillRepository;
    private final String OPEN_ROUTER_URL = "https://openrouter.ai/api/v1/chat/completions";

    private HttpHeaders createOpenRouterHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("HTTP-Referer", "http://localhost:8080"); // Bắt buộc cho OpenRouter
        headers.set("X-Title", "Job Recruitment System");    // Bắt buộc cho OpenRouter
        return headers;
    }

    public OcrResultReponse extractCompanyInfo(MultipartFile file) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        tools.jackson.databind.ObjectMapper mapper = new tools.jackson.databind.ObjectMapper();

        String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
        // Chuẩn Base64 mới cho OpenAI: data:image/jpeg;base64,...
        String dataUrl = "data:" + file.getContentType() + ";base64," + base64Image;

        // --- CẬP NHẬT PROMPT: DẠY AI HIỂU MÃ SỐ DOANH NGHIỆP (GIỮ NGUYÊN PROMPT CŨ CỦA BẠN) ---
        String prompt = "Bạn là hệ thống đọc dữ liệu giấy tờ doanh nghiệp Việt Nam. Hãy trích xuất thông tin từ ảnh này:\n" +
                "1. 'taxCode': Tìm dãy số cạnh dòng chữ 'Mã số doanh nghiệp' HOẶC 'Mã số thuế'. (Lưu ý: Mã số doanh nghiệp chính là Mã số thuế).\n" +
                "2. 'companyName': Tìm tên công ty tiếng Việt đầy đủ  tên doanh nghiệp.\n" +
                "Yêu cầu: Trả về 1 JSON duy nhất: {\"taxCode\": \"...\", \"companyName\": \"...\"}. Nếu không tìm thấy, trả về null.";

        // Tạo Request Body theo chuẩn OpenAI cho OpenRouter
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

        String prompt = "Bạn là chuyên gia nhân sự. Hãy trích xuất thông tin từ CV sau.\n" +
                "Cấu trúc JSON yêu cầu:\n" +
                "{\n" +
                "  \"summary\": \"Tóm tắt ngắn gọn về ứng viên\",\n" +
                "  \"skills\": [\"Kỹ năng 1\", \"Kỹ năng 2\"],\n" +
                "  \"education\": \"Thông tin học vấn\",\n" +
                "  \"experience_years\": số năm kinh nghiệm (kiểu số)\n" +
                "}\n" +
                "YÊU CẦU QUAN TRỌNG: Mảng 'skills' CHỈ ĐƯỢC PHÉP chứa các từ khóa nằm trong danh sách chuẩn sau đây. Hãy tự động đối chiếu và chuẩn hóa kỹ năng trong CV cho khớp với danh sách này: [" + skillListStr + "]. Nếu CV có kỹ năng không khớp hoặc không liên quan, hãy bỏ qua.\n" +
                "Chỉ trả về đúng code JSON, không giải thích gì thêm.";

        Map<String, Object> requestBody = Map.of(
                "model", "google/gemini-2.0-flash-001",
                "messages", List.of(
                        Map.of("role", "user", "content", List.of(
                                Map.of("type", "text", "text", prompt),
                                Map.of("type", "image_url", "image_url", Map.of("url", dataUrl))
                        ))
                )
        );

        // Trả về String JSON trực tiếp để lưu DB
        Map<String, Object> response = sendRequestToOpenRouter(requestBody);
        return extractJsonFromResponse(response);
    }

    public MatchResultReponse calculateMatchScore(String cvJsonData, String jobDescription) {
        String prompt = "Bạn là một chuyên gia nhân sự (HR). Hãy đánh giá mức độ phù hợp giữa Thông tin ứng viên (CV) và Yêu cầu công việc (Job Description) dưới đây.\n\n" +
                "--- THÔNG TIN CV (JSON) ---\n" + cvJsonData + "\n\n" +
                "--- YÊU CẦU CÔNG VIỆC ---\n" + jobDescription + "\n\n" +
                "Yêu cầu:\n" +
                "1. Chấm điểm phù hợp từ 0.0 đến 100.0 (tiêu chí: kỹ năng, kinh nghiệm, học vấn).\n" +
                "2. Đưa ra một câu nhận xét ngắn gọn (dưới 30 chữ) giải thích tại sao lại cho điểm số đó.\n" +
                "3. Trả về DUY NHẤT 1 chuỗi JSON với cấu trúc: {\"score\": 85.5, \"reason\": \"Ứng viên đáp ứng tốt kỹ năng Java nhưng thiếu kinh nghiệm quản lý.\"}";

        Map<String, Object> requestBody = Map.of(
                "model", "openai/gpt-oss-120b", // Dùng con hàng khủng bạn vừa test thành công
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", 0.3 // Giảm nhiệt độ để kết quả ổn định và bám sát prompt
        );

        return callAiAndParseJson(requestBody, MatchResultReponse.class);
    }

    // --- CÁC HÀM HELPER (BỔ TRỢ) ĐỂ CODE SẠCH HƠN ---

    // Gửi Request và trả về Map kết quả thô
    private Map<String, Object> sendRequestToOpenRouter(Map<String, Object> body) {
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, createOpenRouterHeaders());
        return restTemplate.postForObject(OPEN_ROUTER_URL, entity, Map.class);
    }

    // Dùng Regex lấy đúng JSON ra khỏi text (Lấy kết quả OpenAI thô)
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
                    return matcher.group(); // Trả về chuỗi JSON chuẩn
                }
            }
        }
        return "{}"; // Trả về JSON rỗng nếu lỗi
    }

    // Gộp 3 bước: Gọi API -> Lấy JSON -> Chuyển thành Object Java
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