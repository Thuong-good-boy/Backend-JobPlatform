package com.jobplatform.job_recruitment_system.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        return uploadResult.get("secure_url").toString();
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
    public void deleteFile(String fileUrl) {
        try {
            String publicId = getPublicIdFromUrl(fileUrl);
            System.out.println("URL nhận được từ Frontend: " + fileUrl);
            System.out.println("publish Id : " + publicId);
            if (publicId != null) {

                // XỬ LÝ CỐT LÕI: Cắt bỏ đuôi file (.pdf, .jpg,...) vì lúc upload public_id không có đuôi
                if (publicId.contains(".")) {
                    publicId = publicId.substring(0, publicId.lastIndexOf('.'));
                }

                Map<String, Object> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());

                System.out.println(">>> Đã yêu cầu xóa tệp trên Cloudinary: " + publicId + " - Trạng thái: " + result.get("result"));
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi xóa tệp trên Cloudinary: " + e.getMessage());
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
    public String uploadPdfBytes(byte[] fileBytes) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(fileBytes,
                ObjectUtils.asMap(
                        "public_id", "cv_generated_" + UUID.randomUUID().toString(),
                        "resource_type", "image",
                        "format", "pdf"
                ));
        return uploadResult.get("secure_url").toString();
    }


}