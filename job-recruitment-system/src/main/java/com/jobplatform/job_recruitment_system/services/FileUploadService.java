package com.jobplatform.job_recruitment_system.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final Cloudinary cloudinary;

    public String uploadFile(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "public_id", UUID.randomUUID().toString(),
                        "resource_type", "auto"
                ));
        return uploadResult.get("url").toString();
    }


    // Hàm Xóa ảnh
    public void deleteImage(String imageUrl) {
        try {
            String publicId = getPublicIdFromUrl(imageUrl);

            if (publicId != null) {
                // Gọi lên Cloudinary để xóa
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                System.out.println(">>> Đã xóa ảnh trên Cloudinary: " + publicId);
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi xóa ảnh: " + e.getMessage());
        }
    }

    // Hàm tách lấy ID từ Link ảnh (Helper)
    private String getPublicIdFromUrl(String url) {
        try {
            if (!url.contains("cloudinary.com")) {
                return null;
            }
            // Logic của bạn: Cắt từ dấu "/" cuối cùng đến dấu "."
            int beginIndex = url.lastIndexOf("/") + 1;
            int endIndex = url.lastIndexOf(".");

            // Ví dụ: .../image/upload/v123/abcd-1234.jpg
            // -> Lấy được: abcd-1234 (Đây chính là public_id nếu không dùng folder)
            return url.substring(beginIndex, endIndex);
        } catch (Exception e) {
            return null;
        }
    }
}