package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.dtos.CompanyRegisterRequest;
import com.jobplatform.job_recruitment_system.dtos.OcrResult;
import com.jobplatform.job_recruitment_system.models.Company;
import com.jobplatform.job_recruitment_system.models.Role;
import com.jobplatform.job_recruitment_system.models.User;
import com.jobplatform.job_recruitment_system.repositories.CompanyRepository;
import com.jobplatform.job_recruitment_system.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Lưu ý import đúng cái này
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class CompanyService {

    @Autowired private UserRepository userRepository;
    @Autowired private CompanyRepository companyRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private FileUploadService fileUploadService;
    @Autowired private AiOcrService aiOcrService;

    @Transactional
    public void registerCompany(CompanyRegisterRequest request) throws IOException {

        // 1. Check Email trùng
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã tồn tại!");
        }

        // 2. Upload ảnh GPKD lên Cloudinary
        MultipartFile licenseFile = request.getLicenseImage();
        String licenseUrl = fileUploadService.uploadFile(licenseFile);

        try {
            // 3. GỌI AI CHECK (Lấy cả MST và Tên Công Ty)
            OcrResult ocrResult = aiOcrService.extractCompanyInfo(licenseFile);

            // Check null (AI không đọc được gì)
            if (ocrResult == null || ocrResult.getTaxCode() == null || ocrResult.getCompanyName() == null) {
                throw new RuntimeException("Ảnh không rõ nét hoặc không phải Giấy phép kinh doanh hợp lệ.");
            }

            System.out.println("AI Scan -> MST: " + ocrResult.getTaxCode() + " | Tên: " + ocrResult.getCompanyName());

            // --- LOGIC MỚI: SO SÁNH TÊN CÔNG TY (CHỐNG GIAN LẬN) ---
            // Chuyển về chữ thường để so sánh cho chính xác
            String inputName = request.getCompanyName().toLowerCase();
            String aiName = ocrResult.getCompanyName().toLowerCase();

            // Logic: Tên trong ảnh phải CHỨA tên user nhập (hoặc ngược lại)
            // Ví dụ: User nhập "FPT" - Ảnh có "Công ty FPT" -> OK
            if (!aiName.contains(inputName) && !inputName.contains(aiName)) {
                throw new RuntimeException("Thông tin không khớp! Tên công ty trong ảnh (" + ocrResult.getCompanyName()
                        + ") khác với tên bạn nhập (" + request.getCompanyName() + ").");
            }

            // 4. Kiểm tra xem MST này đã đăng ký trong hệ thống chưa
            if (companyRepository.existsByTaxCode(ocrResult.getTaxCode())) {
                throw new RuntimeException("Công ty với Mã số thuế này đã tồn tại trong hệ thống!");
            }

            // 5. Tạo User
            User user = new User();
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setFullName(request.getFullName());
            user.setRole(Role.COMPANY); // Đã sửa thành Role.COMPANY
            user.setActive(true);
            User savedUser = userRepository.save(user);

            // 6. Tạo Company Profile
            Company company = new Company();
            company.setUser(savedUser);
            company.setCompanyName(request.getCompanyName()); // Lưu tên user nhập (đã qua kiểm duyệt khớp với ảnh)
            company.setAddress(request.getAddress());
            company.setTaxCode(ocrResult.getTaxCode()); // Lưu MST chính xác từ ảnh
            company.setVerified(true);

            // Xóa URL ảnh để bảo mật
            company.setLicenseImageUrl(null);

            companyRepository.save(company);

        } catch (Exception e) {
            // Nếu có lỗi bất kỳ (Validation sai, AI lỗi, DB lỗi...) -> Xóa ảnh ngay
            fileUploadService.deleteImage(licenseUrl);
            throw e; // Ném lỗi ra để Controller bắt
        }

        // 7. Xóa ảnh sau khi thành công (Double check)
        fileUploadService.deleteImage(licenseUrl);
        System.out.println(">>> Đăng ký thành công & Đã xóa ảnh bảo mật.");
    }
}