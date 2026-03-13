package com.jobplatform.job_recruitment_system.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.jobplatform.job_recruitment_system.models.Role;
import com.jobplatform.job_recruitment_system.models.User;
import com.jobplatform.job_recruitment_system.repositories.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.Random;


@Service
public class UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;

    @Value("${google.client.id}")
    private  String googleClientId;

    @Autowired
    private JwtService jwtService;

    public User registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã tồn tại!");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.CANDIDATE);
        user.setAuthProvider("LOCAL");
        String otp = String.valueOf(new Random().nextInt(900000) + 100000); // Random 6 số
        user.setVerificationCode(otp);
        user.setVerificationExpiration(LocalDateTime.now().plusMinutes(5)); // Hết hạn sau 5p
        user.setActive(false); // Chưa kích hoạt
        User savedUser = userRepository.save(user);


        try {
            emailService.sendOtpEmail(user.getEmail(), otp);
        } catch (MessagingException e) {
            e.printStackTrace(); // Xử lý lỗi gửi mail
        }

        return savedUser;
    }
    public void resendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email này!"));

        if (user.isActive()) {
            throw new RuntimeException("Tài khoản này đã được kích hoạt trước đó rồi.");
        }

        String newOtp = String.valueOf(new Random().nextInt(900000) + 100000);

        user.setVerificationCode(newOtp);
        user.setVerificationExpiration(LocalDateTime.now().plusMinutes(5));

        userRepository.save(user);

        try {
            emailService.sendOtpEmail(user.getEmail(), newOtp);
        } catch (MessagingException e) {
            throw new RuntimeException("Lỗi khi gửi lại email: " + e.getMessage());
        }
    }
    public void verifyUser(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user!"));

        if (user.isActive()) {
            throw new RuntimeException("Tài khoản đã được kích hoạt trước đó!");
        }

        if (user.getVerificationExpiration().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Mã OTP đã hết hạn!");
        }

        if (user.getVerificationCode().equals(otp)) {
            user.setActive(true);
            user.setVerificationCode(null);
            userRepository.save(user);
        } else {
            throw new RuntimeException("Mã OTP không chính xác!");
        }
    }
    public Optional<User> findByEmail(String email){
        return  userRepository.findByEmail(email);
    }
    public void Save(User user){
        userRepository.save(user);
        return ;
    }
    public  String loginWithGoogle(String credential, String role) throws IOException, GeneralSecurityException {
        // 1. Cấu hình bộ xác thực của Google
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();
        // 2. Xác thực token gửi từ Frontend
        GoogleIdToken idToken = verifier.verify(credential);

        if (idToken == null) {
            throw new RuntimeException("Token Google không hợp lệ!");
        }

        // 3. Lấy thông tin người dùng từ Google
        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            // User mới tinh -> Tạo mới
            user = new User();
            user.setEmail(email);
            user.setFullName(payload.get("name").toString());
            user.setAuthProvider("GOOGLE");
            user.setActive(true);
            try {
                user.setRole(role != null ? Role.valueOf(role) : Role.CANDIDATE);
            } catch (IllegalArgumentException e) {
                user.setRole(Role.CANDIDATE);
            }
            userRepository.save(user);
        } else {

            if (!"GOOGLE".equals(user.getAuthProvider())) {
                user.setAuthProvider("GOOGLE");
                userRepository.save(user);
            }
        }

        return jwtService.generateToken(user.getEmail());
    }
    public  User getReferenceById(Long userId){
        return  userRepository.getReferenceById(userId);
    }
}
