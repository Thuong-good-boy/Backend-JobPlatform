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


    public void deleteImage(String imageUrl) {
        try {
            String publicId = getPublicIdFromUrl(imageUrl);

            if (publicId != null) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                System.out.println(">>> Đã xóa ảnh trên Cloudinary: " + publicId);
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi xóa ảnh: " + e.getMessage());
        }
    }

    private String getPublicIdFromUrl(String url) {
        try {
            if (!url.contains("cloudinary.com")) {
                return null;
            }
            int beginIndex = url.lastIndexOf("/") + 1;
            int endIndex = url.lastIndexOf(".");

            return url.substring(beginIndex, endIndex);
        } catch (Exception e) {
            return null;
        }
    }
}