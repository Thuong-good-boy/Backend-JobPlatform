package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.exceptions.AppException;
import com.jobplatform.job_recruitment_system.exceptions.ErrorCode;
import com.jobplatform.job_recruitment_system.models.RefreshToken;
import com.jobplatform.job_recruitment_system.repositories.RefreshTokenRepository;
import com.jobplatform.job_recruitment_system.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    // Lấy thời gian hết hạn từ file application.properties (ví dụ: 604800000 cho 7 ngày)
    @Value("${jwt.refresh-expiration}")
    private Long refreshTokenDurationMs;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Hàm 1: Tạo Refresh Token mới khi đăng nhập
     */
    public RefreshToken createRefreshToken(String email) {
        RefreshToken refreshToken = new RefreshToken();

        // 1. Tìm user
        refreshToken.setUser(userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy User")));

        // 2. Tạo chuỗi Token
        // Chú ý: Với Refresh Token lưu DB, dùng UUID (chuỗi ngẫu nhiên) là an toàn và tối ưu nhất
        // thay vì mã hóa cả một cục JWT dài dòng.
        refreshToken.setToken(UUID.randomUUID().toString());

        // 3. Set thời gian hết hạn (Tính từ thời điểm hiện tại + số mili giây cấu hình)
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));

        // 4. Lưu vào SQL
        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Hàm 2: Tìm token trong DB
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Hàm 3: Kiểm tra xem token gửi lên đã hết hạn chưa
     */
    public RefreshToken verifyExpiration(RefreshToken token) {
        // Nếu ngày hết hạn (ExpiryDate) nhỏ hơn thời gian hiện tại (Instant.now()) -> Đã hết hạn
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            // Xóa khoi  DB
            refreshTokenRepository.delete(token);
            throw new AppException(ErrorCode.APP_001);
        }
        return token;
    }

    /**
     * Hàm 4: Dùng cho tính năng Đăng xuất (Logout)
     */
    public void deleteByUserId(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            refreshTokenRepository.deleteByUser(user);
        });
    }
}