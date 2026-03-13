package com.jobplatform.job_recruitment_system.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    // Lưu OTP với thời gian hết hạn (Time To Live - TTL)
    public void saveOtp(String email, String otp, long timeoutInMinutes) {
        String key = "FORGOT_PASSWORD:" + email; // Tạo key để phân biệt

        // Lưu vào Redis: Key - Value - Thời gian tồn tại - Đơn vị thời gian
        redisTemplate.opsForValue().set(key, otp, timeoutInMinutes, TimeUnit.MINUTES);
    }
    // Lấy OTP ra để check
    public String getOtp(String email) {
        String key = "FORGOT_PASSWORD:" + email;
        return redisTemplate.opsForValue().get(key);
    }
    // Xóa OTP sau khi dùng xong (để không dùng lại được)
    public void deleteOtp(String email) {
        String key = "FORGOT_PASSWORD:" + email;
        redisTemplate.delete(key);
    }

    // Kiểm tra xem user có đang yêu cầu OTP không
    public boolean hasOtp(String email) {
        String key = "FORGOT_PASSWORD:" + email;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    // Trong RedisService.java
    public void saveResetToken(String email, String token) {
        // Lưu token đổi pass
        redisTemplate.opsForValue().set("RESET_TOKEN:" + email, token, 5, TimeUnit.MINUTES);
    }

    public String getResetToken(String email) {
        return redisTemplate.opsForValue().get("RESET_TOKEN:" + email);
    }

    public void deleteResetToken(String email) {
        redisTemplate.delete("RESET_TOKEN:" + email);
    }
}
