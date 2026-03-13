package com.jobplatform.job_recruitment_system.services;


import com.jobplatform.job_recruitment_system.dtos.MatchResult;
import com.jobplatform.job_recruitment_system.dtos.OcrResult;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AiOcrService {

    @Value("${gemini.api-key}")
    private String apiKey;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";



    public OcrResult extractCompanyInfo(MultipartFile file) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        tools.jackson.databind.ObjectMapper mapper = new tools.jackson.databind.ObjectMapper();

        String base64Image = Base64.getEncoder().encodeToString(file.getBytes());

        // --- CẬP NHẬT PROMPT: DẠY AI HIỂU MÃ SỐ DOANH NGHIỆP ---
        String prompt = "Bạn là hệ thống đọc dữ liệu giấy tờ doanh nghiệp Việt Nam. Hãy trích xuất thông tin từ ảnh này:\n" +
                "1. 'taxCode': Tìm dãy số cạnh dòng chữ 'Mã số doanh nghiệp' HOẶC 'Mã số thuế'. (Lưu ý: Mã số doanh nghiệp chính là Mã số thuế).\n" +
                "2. 'companyName': Tìm tên công ty tiếng Việt đầy đủ (thường ở dòng 'Tên công ty viết bằng tiếng Việt' hoặc dòng chữ in hoa lớn nhất).\n" +
                "Yêu cầu: Trả về 1 JSON duy nhất: {\"taxCode\": \"...\", \"companyName\": \"...\"}. Nếu không tìm thấy, trả về null.";

        GeminiRequest request = new GeminiRequest();
        GeminiRequest.Part textPart = new GeminiRequest.Part();
        textPart.text = prompt;

        GeminiRequest.Part imagePart = new GeminiRequest.Part();
        imagePart.inline_data = new GeminiRequest.InlineData();
        imagePart.inline_data.mime_type = file.getContentType();
        imagePart.inline_data.data = base64Image;

        request.contents = Collections.singletonList(new GeminiRequest.Content(List.of(textPart, imagePart)));

        try {
            GeminiResponse response = restTemplate.postForObject(GEMINI_API_URL + apiKey, request, GeminiResponse.class);
//            {
//                "candidates": [
//                {
//                    "content": {
//                    "parts": [
//                    {"text": "```json\n{...}\n```"}
//        ]
//                }
//                }
//  ]
//            }
            if (response != null && response.candidates != null && !response.candidates.isEmpty()) {
                String rawText = response.candidates.get(0).content.parts.get(0).text;

                System.out.println(">>> AI Raw Response: " + rawText);
                // đây là Tuyệt chiêu Regex
                Pattern pattern = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
                Matcher matcher = pattern.matcher(rawText);

                if (matcher.find()) {
                    //convert JSON → Object Java
                    String jsonString = matcher.group();
                    return mapper.readValue(jsonString, OcrResult.class);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(">>> Lỗi AI: " + e.getMessage());
        }
        return null;
    }

    public String extractCvInfoToJson(MultipartFile file) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        String base64Image = Base64.getEncoder().encodeToString(file.getBytes());

        String prompt = "Bạn là chuyên gia nhân sự. Hãy đọc ảnh CV này và trích xuất thông tin thành JSON.\n" +
                "Cấu trúc JSON yêu cầu:\n" +
                "{\n" +
                "  \"summary\": \"Tóm tắt ngắn gọn về ứng viên\",\n" +
                "  \"skills\": [\"Kỹ năng 1\", \"Kỹ năng 2\"],\n" +
                "  \"education\": \"Thông tin học vấn\",\n" +
                "  \"experience_years\": số năm kinh nghiệm (kiểu số)\n" +
                "}\n" +
                "Yêu cầu: Chỉ trả về đúng code JSON, không giải thích gì thêm.";

        // Gửi request tới Gemini (tận dụng lại DTO GeminiRequest bạn đã có)
        GeminiRequest request = new GeminiRequest();
        GeminiRequest.Part textPart = new GeminiRequest.Part();
        textPart.text = prompt;

        GeminiRequest.Part imagePart = new GeminiRequest.Part();
        imagePart.inline_data = new GeminiRequest.InlineData();
        imagePart.inline_data.mime_type = file.getContentType();
        imagePart.inline_data.data = base64Image;

        request.contents = Collections.singletonList(new GeminiRequest.Content(List.of(textPart, imagePart)));

        try {
            GeminiResponse response = restTemplate.postForObject(GEMINI_API_URL + apiKey, request, GeminiResponse.class);
            if (response != null && !response.candidates.isEmpty()) {
                String rawText = response.candidates.get(0).content.parts.get(0).text;

                // Dùng Regex lấy phần JSON để lưu vào DB
                Pattern pattern = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
                Matcher matcher = pattern.matcher(rawText);
                if (matcher.find()) {
                    return matcher.group(); // Trả về chuỗi JSON để lưu vào cột cv_data
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi AI đọc CV: " + e.getMessage());
        }
        return "{}"; // Trả về JSON rỗng nếu lỗi
    }
    public MatchResult calculateMatchScore(String cvJsonData, String jobDescription) {
        RestTemplate restTemplate = new RestTemplate();
        tools.jackson.databind.ObjectMapper mapper = new tools.jackson.databind.ObjectMapper();


        String prompt = "Bạn là một chuyên gia nhân sự (HR). Hãy đánh giá mức độ phù hợp giữa Thông tin ứng viên (CV) và Yêu cầu công việc (Job Description) dưới đây.\n\n" +
                "--- THÔNG TIN CV (JSON) ---\n" + cvJsonData + "\n\n" +
                "--- YÊU CẦU CÔNG VIỆC ---\n" + jobDescription + "\n\n" +
                "Yêu cầu:\n" +
                "1. Chấm điểm phù hợp từ 0.0 đến 100.0 (tiêu chí: kỹ năng, kinh nghiệm, học vấn).\n" +
                "2. Đưa ra một câu nhận xét ngắn gọn (dưới 30 chữ) giải thích tại sao lại cho điểm số đó.\n" +
                "3. Trả về DUY NHẤT 1 chuỗi JSON với cấu trúc: {\"score\": 85.5, \"reason\": \"Ứng viên đáp ứng tốt kỹ năng Java nhưng thiếu kinh nghiệm quản lý.\"}";

        // Tạo Request gửi đi (Chỉ có Text, KHÔNG CÓ Image)
        GeminiRequest request = new GeminiRequest();
        GeminiRequest.Part textPart = new GeminiRequest.Part();
        textPart.text = prompt;

        // Không set inline_data vì không có ảnh

        request.contents = Collections.singletonList(new GeminiRequest.Content(List.of(textPart)));

        try {
            GeminiResponse response = restTemplate.postForObject(GEMINI_API_URL + apiKey, request, GeminiResponse.class);

            if (response != null && response.candidates != null && !response.candidates.isEmpty()) {
                String rawText = response.candidates.get(0).content.parts.get(0).text;
                System.out.println(">>> AI Matching Response: " + rawText);

                // Dùng Regex trích xuất JSON
                Pattern pattern = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
                Matcher matcher = pattern.matcher(rawText);

                if (matcher.find()) {
                    String jsonString = matcher.group();
                    return mapper.readValue(jsonString, MatchResult.class);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(">>> Lỗi AI Matching: " + e.getMessage());
        }

        // Nếu lỗi, trả về điểm 0
        MatchResult defaultResult = new MatchResult();
        defaultResult.setScore(0.0);
        defaultResult.setReason("Không thể đánh giá vào lúc này.");
        return defaultResult;
    }

    @Data
    public static class GeminiRequest {
        List<Content> contents;
        @Data static class Content { List<Part> parts; public Content(List<Part> parts) { this.parts = parts; } }
        @Data static class Part { String text; InlineData inline_data; }
        @Data static class InlineData { String mime_type; String data; }
    }
    @Data
    public static class GeminiResponse {
        List<Candidate> candidates;
        @Data static class Candidate { Content content; }
        @Data static class Content { List<Part> parts; }
        @Data static class Part { String text; }
    }
}