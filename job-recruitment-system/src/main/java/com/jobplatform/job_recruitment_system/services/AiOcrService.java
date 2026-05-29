package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.dtos.Response.AiBreakdownResultResponse;
import com.jobplatform.job_recruitment_system.dtos.Response.MatchResultReponse;
import com.jobplatform.job_recruitment_system.dtos.Response.OcrResultReponse;
import com.jobplatform.job_recruitment_system.models.Job;
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
import java.io.InputStream;
import java.net.URL;
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
    private final SkillRepository skillRepository;
    private final String OPEN_ROUTER_URL = "https://openrouter.ai/api/v1/chat/completions";

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
    public String evaluateCvFromUrl(String fileUrl) throws IOException {
        if (fileUrl.contains("cloudinary.com") && fileUrl.endsWith(".pdf")) {
            fileUrl = fileUrl.replace(".pdf", ".jpg");
        }

        byte[] fileBytes;
        URL url = new URL(fileUrl);
        try (InputStream in = url.openStream()) {
            fileBytes = in.readAllBytes();
        }

        String mimeType = fileUrl.endsWith(".pdf") ? "application/pdf" : "image/jpeg";
        String base64Image = Base64.getEncoder().encodeToString(fileBytes);
        String dataUrl = "data:" + mimeType + ";base64," + base64Image;

        String prompt = "Bạn là một Headhunter kiêm chuyên gia thiết kế CV. Hãy nhìn vào hình ảnh CV này và soi thật kỹ.\n" +
                "Đánh giá chi tiết hình thức (bố cục lề, màu sắc, font chữ lớn/nhỏ, lỗi chính tả/ngữ pháp) và nội dung.\n" +
                "Trả về kết quả dưới dạng JSON sau:\n" +
                "{\n" +
                "  \"pros\": [\"Điểm mạnh 1\", \"Điểm mạnh 2\"],\n" +
                "  \"cons\": [\"Điểm yếu 1 (VD: Cỡ chữ quá nhỏ, sai chính tả ở dòng X, màu nhạt...)\", \"Điểm yếu 2\"],\n" +
                "  \"suggestions\": [\"Gợi ý 1 (VD: Nên đổi sang font Serif, canh lề lại...)\", \"Gợi ý 2\"]\n" +
                "}\n" +
                "YÊU CẦU QUAN TRỌNG: Chỉ trả về đúng JSON hợp lệ, không bọc trong Markdown, không giải thích thêm.";

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