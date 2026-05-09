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

        refreshToken.setUser(userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy User")));
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
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