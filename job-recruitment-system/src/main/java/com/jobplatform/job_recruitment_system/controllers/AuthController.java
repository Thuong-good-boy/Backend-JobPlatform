package com.jobplatform.job_recruitment_system.controllers;


import com.jobplatform.job_recruitment_system.dtos.CompanyRegisterRequest;
import com.jobplatform.job_recruitment_system.models.ResetPasswordRequest;
import com.jobplatform.job_recruitment_system.models.User;
import com.jobplatform.job_recruitment_system.models.VerifyOtpRequest;
import com.jobplatform.job_recruitment_system.services.*;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager; // Import cái này
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Import cái này
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CompanyService companyService;
    @Autowired
    private EmailService emailService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private PasswordEncoder passwordEncoder;





    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            User newUser = userService.registerUser(user);
            return ResponseEntity.ok(newUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        try {
            //  Nhờ AuthenticationManager xác thực
            // Nó sẽ tự gọi UserDetailsService và PasswordEncoder để check
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            // Nếu check thành công (không bị lỗi), thì code chạy tiếp xuống đây
            // Lấy thông tin user để trả về (đoạn này dùng lại code cũ của bạn cho đúng format)
            User user = userService.findByEmail(email).orElseThrow();
            String token = jwtService.generateToken(email);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            Map<String, Object> userData = new HashMap<>();
            userData.put("id",user.getId());
            userData.put("email", user.getEmail());
            userData.put("fullname",user.getFullName());
            userData.put("role", user.getRole());
            response.put("user",userData);
            response.put("message", "Đăng nhập thành công!");

            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            // Nếu sai pass hoặc không tìm thấy user, nó sẽ nhảy vào đây
            return ResponseEntity.status(401).body("Sai email hoặc mật khẩu!");
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");

        try {
            userService.verifyUser(email, otp);
            return ResponseEntity.ok("Xác thực thành công! Bạn có thể đăng nhập ngay.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestParam String email) {
        try {
            userService.resendOtp(email);
            return ResponseEntity.ok("Mã OTP mới đã được gửi vào email của bạn.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(value = "/check/company", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerCompany(@ModelAttribute CompanyRegisterRequest request) {
        try {
            companyService.registerCompany(request);

            return ResponseEntity.ok("Đăng ký Doanh nghiệp thành công! AI đã xác thực GPKD của bạn.");
        } catch (Exception e) {
            e.printStackTrace(); // In lỗi ra console để debug
            return ResponseEntity.badRequest().body("Lỗi đăng ký: " + e.getMessage());
        }
    }
    @PostMapping("/forgot-password-verify")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest request) {
        // 1. Lấy OTP từ Redis
        String savedOtp = redisService.getOtp(request.getEmail());

        // 2. Kiểm tra
        if (savedOtp == null || !savedOtp.equals(request.getOtp())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("OTP sai hoặc đã hết hạn!");
        }

        // 3. OTP đúng -> Tạo "Vé đổi mật khẩu" (Reset Token)
        String resetToken = UUID.randomUUID().toString(); // Tạo chuỗi ngẫu nhiên

        // 4. Lưu vé này vào Redis để lát nữa check
        redisService.saveResetToken(request.getEmail(), resetToken);

        // 5. Xóa OTP đi
        redisService.deleteOtp(request.getEmail());

        // 6. Trả vé về cho Client
        Map<String, String> response = new HashMap<>();
        response.put("message", "OTP hợp lệ");
        response.put("resetToken", resetToken);

        return ResponseEntity.ok(response);
    }
    @PostMapping("/forgot-password")
    public void forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email").trim() ;
        // 1. Kiểm tra email có tồn tại trong DB không
        User user = userService.findByEmail(email)
                .orElseThrow();

        String otp = String.valueOf(new Random().nextInt(900000) + 100000); // Hàm tự viết (random 6 số)

        // 3. LƯU VÀO REDIS (Thay vì lưu vào DB)
        // Key: email, Value: otp, Hết hạn: 5 phút
        redisService.saveOtp(email, otp, 5);

        // 4. Gửi email cho user
        try {
            emailService.sendForgotPasswordEmail(email, otp);
        }catch (MessagingException e) {
            throw new RuntimeException("Gửi email thất bại", e);
        }

    }
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {


        // 2. KIỂM TRA "VÉ"
        String savedToken = redisService.getResetToken(request.getEmail());

        if (savedToken == null || !savedToken.equals(request.getResetToken())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Phiên đổi mật khẩu đã hết hạn hoặc không hợp lệ!");
        }

        // 3. Mọi thứ OK -> Update Database
        User user = userService.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User không tìm thấy"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userService.Save(user);

        // 4. Xóa "Vé" đi
        redisService.deleteResetToken(request.getEmail());

        return ResponseEntity.ok("Đổi mật khẩu thành công! Vui lòng đăng nhập lại.");
    }
    @PostMapping("/login-google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> body) {
        try {
            String token = userService.loginWithGoogle(body.get("credential"), body.get("role"));
            return ResponseEntity.ok(Collections.singletonMap("token", token));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Lỗi đăng nhập Google");
        }
    }





}