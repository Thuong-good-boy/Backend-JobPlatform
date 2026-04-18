package com.jobplatform.job_recruitment_system.repositories;

import com.jobplatform.job_recruitment_system.models.RefreshToken;
import com.jobplatform.job_recruitment_system.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // Tìm kiếm token trong DB xem có tồn tại không
    Optional<RefreshToken> findByToken(String token);

    // Dùng để xóa token khi người dùng bấm Đăng Xuất (Logout)
    @Modifying
    @Transactional
    int deleteByUser(User user);
}