package com.jobplatform.job_recruitment_system.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.jobplatform.job_recruitment_system.dtos.Response.LoginResponse;
import com.jobplatform.job_recruitment_system.dtos.request.*;
import com.jobplatform.job_recruitment_system.exceptions.AppException;
import com.jobplatform.job_recruitment_system.exceptions.ErrorCode;
import com.jobplatform.job_recruitment_system.models.RefreshToken;
import com.jobplatform.job_recruitment_system.models.Role;
import com.jobplatform.job_recruitment_system.models.User;
import com.jobplatform.job_recruitment_system.repositories.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.jobplatform.job_recruitment_system.mapper.UserMapper;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RedisService redisService;
    private final  AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    @Value("${google.client.id}")
    private String googleClientId;

    public LoginResponse login(LoginRequest request) {
        // 1. Xác thực bằng AuthenticationManager
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new AppException(ErrorCode.AUTH_005);
        }

        // 2. Lấy user từ DB
        User user = findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_005));

        // 3. Kiểm tra Role
        if (!user.getRole().toString().trim().equalsIgnoreCase(request.getRole().trim())) {
            throw new AppException(ErrorCode.AUTH_006);
        }
        LoginResponse response = userMapper.toLoginResponse(user);
        // 4. Tạo token
        response.setAccessToken(jwtService.generateAccessToken(request.getEmail()));
        response.setRefreshToken(refreshTokenService.createRefreshToken(request.getEmail()).getToken());
        // 5. Trả về DTO
        return response;
    }
    public String refreshToken(String requestRefreshToken) {
        if (requestRefreshToken == null || requestRefreshToken.isBlank()) {
            throw new AppException(ErrorCode.AUTH_002);
        }
        // 1. Tìm token trong cơ sở dữ liệu
        RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken)
                .orElseThrow(() -> new AppException(ErrorCode.AUTH_002));

        // 2. Kiểm tra hạn
        refreshTokenService.verifyExpiration(refreshToken);

        // 3. Lấy thông tin User và tạo Access Token mới
        User user = refreshToken.getUser();

        String newAccessToken = jwtService.generateAccessToken(user.getEmail());

        return newAccessToken;
    }

    public void registerUser(RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new AppException(ErrorCode.USER_001);
        }

        // 2. Tạo OTP 6 số
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        try {
            // 3. Biến Object User thành chuỗi JSON để ném vào Redis
            ObjectMapper mapper = new ObjectMapper();
            String userJson = mapper.writeValueAsString(registerRequest);


            // Lưu thông tin User và OTP vào Redis (Hết hạn sau 5 phút)
            redisService.saveData("REG_DATA:" + registerRequest.getEmail(), userJson, 5);
            redisService.saveData("REG_OTP:" + registerRequest.getEmail(), otp, 5);
        } catch (Exception e) {
            throw new AppException(ErrorCode.USER_011);
        }

        try {
            emailService.sendOtpEmail(registerRequest.getEmail(), otp);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.USER_009);
        }
    }
    public LoginResponse verifyAndLogin(VerifyOtpRequest request) {
        verifyUser(request.getEmail(), request.getOtp());

        User user = findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.AUTH_004));

        LoginResponse response = userMapper.toLoginResponse(user);

        response.setAccessToken(jwtService.generateAccessToken(user.getEmail()));
        response.setRefreshToken(refreshTokenService.createRefreshToken(user.getEmail()).getToken());

        return response;
    }
    public void verifyUser(String email, String inputOtp) {
        // 1. Móc mã OTP từ Redis ra đối chiếu
        String redisOtp = redisService.getData("REG_OTP:" + email);
        if (redisOtp == null || !redisOtp.equals(inputOtp)) {
            throw new AppException(ErrorCode.USER_006);
        }

        String userJson = redisService.getData("REG_DATA:" + email);
        if (userJson == null) {
            throw new AppException(ErrorCode.USER_007);
        }

        try {
            // 3. Đọc JSON thành DTO RegisterRequest
            ObjectMapper mapper = new ObjectMapper();
            RegisterRequest request = mapper.readValue(userJson, RegisterRequest.class);

            // 4. MAPPER CHẠY Ở ĐÂY: Biến DTO thành Entity
            User user = userMapper.toEntity(request);

            // Cấu hình các trường bảo mật
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setAuthProvider("LOCAL");

            // Lưu vào DB
            userRepository.save(user);

            // 5. Quét dọn rác trong Redis
            redisService.delete("REG_OTP:" + email);
            redisService.delete("REG_DATA:" + email);

        } catch (Exception e) {
            throw new AppException(ErrorCode.USER_004);
        }
    }
    public Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }

        String email = auth.getName();

        User user = userRepository.findByEmail(email).orElse(null);

        return (user != null) ? user.getId() : null;
    }

    public void resendOtp(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new AppException(ErrorCode.USER_002);
        }

        String userJson = redisService.getData("REG_DATA:" + email);
        if (userJson == null) {
            throw new AppException(ErrorCode.USER_008);
        }

        String newOtp = String.valueOf(new Random().nextInt(900000) + 100000);

        try {
            redisService.saveData("REG_PASSWORD:" + email, newOtp, 5);
            redisService.saveData("REG_DATA:" + email, userJson, 5);
        } catch (Exception e) {
            throw new AppException(ErrorCode.USER_011);
        }

        try {
            emailService.sendOtpEmail(email, newOtp);
        } catch (Exception e) {
            throw new AppException(ErrorCode.USER_010);
        }
    }
    public String verifyOtp(VerifyOtpRequest request){
        String savedOtp = redisService.getData( "FORGOT_PASSWORD:" +request.getEmail());
        System.out.println(savedOtp+" lalal "+ request.getOtp());
        if (savedOtp == null || !savedOtp.equals(request.getOtp())) {
            throw new AppException(ErrorCode.USER_006);
        }
        String resetToken = UUID.randomUUID().toString();
        try {

            redisService.saveData("RESET_TOKEN:" + resetToken,request.getEmail(), 15);
            redisService.delete("FORGOT_PASSWORD:" + request.getEmail());

        }catch (Exception e){
            throw  new AppException(ErrorCode.USER_011);
        }
        return resetToken;


    }
    public void processForgotPassword(String email) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_003));

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        try {
            redisService.saveData("FORGOT_PASSWORD:" + email, otp, 5);
        } catch (Exception e) {
            throw new AppException(ErrorCode.USER_011);
        }

        try {
            emailService.sendForgotPasswordEmail(email, otp);
        } catch (Exception e) {
            throw new AppException(ErrorCode.USER_009);
        }
    }
    public void resetPassword(ResetPasswordRequest request) {
        String email = redisService.getData("RESET_TOKEN:" + request.getResetToken());
        System.out.println(email+" resettoken : "+ request.getResetToken());
        if (email == null) {
            throw new AppException(ErrorCode.AUTH_007);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_005));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        redisService.delete("RESET_TOKEN:" + request.getResetToken());
    }
    public Optional<User> findByEmail(String email){
        return userRepository.findByEmail(email);
    }

    public void Save(User user){
        userRepository.save(user);
    }

    public User getReferenceById(Long userId){
        return userRepository.getReferenceById(userId);
    }

    public Map<String, Object> loginWithGoogle(String credential, String role) throws IOException, GeneralSecurityException {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();
        GoogleIdToken idToken = verifier.verify(credential);

        if (idToken == null) {
            throw new RuntimeException("Token Google không hợp lệ!");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setFullName(payload.get("name").toString());
            user.setAuthProvider("GOOGLE");
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

        String accessToken = jwtService.generateAccessToken(user.getEmail());
        RefreshToken refreshTokenObj = refreshTokenService.createRefreshToken(user.getEmail());

        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshTokenObj.getToken());

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("email", user.getEmail());
        userData.put("fullname", user.getFullName());
        userData.put("role", user.getRole());

        result.put("user", userData);

        return result;
    }
    private GoogleIdToken.Payload verifyGoogleCredential(String credential) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(credential);
            if (idToken == null) {
                throw new AppException(ErrorCode.AUTH_003);
            }
            return idToken.getPayload();
        } catch (Exception e) {
            throw new AppException(ErrorCode.AUTH_003);
        }
    }

    public LoginResponse loginWithGoogle(GoogleLoginRequest request) {

        GoogleIdToken.Payload payload = verifyGoogleCredential(request.getCredential());
        String email = payload.getEmail();

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setFullName((String) payload.get("name"));
            user.setAuthProvider("GOOGLE");
            user.setRole(Role.valueOf(request.getRole()));

            user = userRepository.save(user);
        } else {
            if (!"GOOGLE".equals(user.getAuthProvider())) {
                user.setAuthProvider("GOOGLE");
                user = userRepository.save(user);
            }
        }
        LoginResponse response = userMapper.toLoginResponse(user);
        response.setAccessToken(jwtService.generateAccessToken(user.getEmail()));
        response.setRefreshToken(refreshTokenService.createRefreshToken(user.getEmail()).getToken());
        return response;
    }
    public  Optional<User> getUserId(Long userId){
        return  userRepository.findById(userId);
    }
}