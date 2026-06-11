package com.jobplatform.job_recruitment_system.controllers;


import com.jobplatform.job_recruitment_system.dtos.Response.LoginResponse;
import com.jobplatform.job_recruitment_system.dtos.Response.UserResponse;
import com.jobplatform.job_recruitment_system.dtos.request.*;
import com.jobplatform.job_recruitment_system.exceptions.AppException;
import com.jobplatform.job_recruitment_system.exceptions.ErrorCode;
import com.jobplatform.job_recruitment_system.models.User;
import com.jobplatform.job_recruitment_system.services.*;
import com.jobplatform.job_recruitment_system.utils.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private  final  LoginAttemptService loginAttemptService;
    private  final CaptchaService captchaService;
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest userRequest, HttpServletRequest request) {
        try {
            LoginResponse userResponse = userService.login(userRequest,request);
            ResponseCookie cookie = CookieUtils.createRefreshTokenCookie(userResponse.getRefreshToken(),userRequest.isRemember());
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(Map.of("accessToken",userResponse.getAccessToken()

                            ));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Sai email hoặc mật khẩu!"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue(name = "refreshToken", required = false) String refreshToken) {

            String accessNewToken= userService.refreshToken(refreshToken);

        return ResponseEntity.ok(Map.of(
                "accessToken", accessNewToken
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            userService.registerUser(registerRequest);
            return ResponseEntity.ok(Map.of("message", "Đăng ký thành công! Vui lòng kiểm tra email để lấy mã OTP."));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping("role")
    public ResponseEntity<?> getRole(){
        return ResponseEntity.ok(userService.getCurrentUserRode());
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@Valid @RequestBody VerifyOtpRequest request) {
        LoginResponse response = userService.verifyAndLogin(request);
        ResponseCookie cookie = CookieUtils.createRefreshTokenCookie(response.getRefreshToken(),false);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of(
                        "accessToken", response.getAccessToken()
                ));
    }
    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestParam String email) {
            userService.resendOtp(email);
            return ResponseEntity.ok("Mã OTP mới đã được gửi vào email của bạn.");

    }
    @PostMapping("/resendRegister-otp")
    public ResponseEntity<?> resendRegisterOtp(@RequestBody Map<String, String>  request) {
        userService.resendOtpRegister(request.get("email"));
        return ResponseEntity.ok("Mã OTP mới đã được gửi vào email của bạn.");

    }


    @PostMapping("/forgot-password-verify")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {

        String resetToken=userService.verifyOtp(request);

        return ResponseEntity.ok(Map.of("message","Xác thực OTP thành công",
                                        "resetToken",resetToken));
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        userService.processForgotPassword(request.getEmail());
        return ResponseEntity.ok(Map.of("message","Mã OTP đã được gửi đến email của bạn."));

    }
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        System.out.println(request.getResetToken()+ " new pass "+ request.getNewPassword());
        userService.resetPassword(request);
        return  ResponseEntity.ok(Map.of("message","Đổi mật khẩu thành công! Vui lòng đăng nhập lại."));
    }

    @PostMapping("/login-google")
    public ResponseEntity<?> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        LoginResponse response = userService.loginWithGoogle(request);
        ResponseCookie cookie = CookieUtils.createRefreshTokenCookie(response.getRefreshToken(),false);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of(
                        "accessToken", response.getAccessToken(),
                        "isNew", response.isNew()
                ));

    }

    @GetMapping("/current")
    public  ResponseEntity<?> getUserCurrent(){
        Long userId = userService.getCurrentUserId();
        User user = userService.getUserId(userId).orElseThrow(()-> new AppException(ErrorCode.AUTH_008));
        Map<String, Object> response = new HashMap<>();
        response.put("fullName", user.getFullName());
        response.put("role", user.getRole());
        response.put("avatarUrl", user.getAvatarUrl());
        return ResponseEntity.ok(response);
    }
    @GetMapping("/currentid")
    public  ResponseEntity<?> getUserCurrentId(){
        Long userId= userService.getCurrentUserId();
        return  ResponseEntity.ok(Map.of("userId",userId));
    }
    @GetMapping("/me")
    public ResponseEntity<?> getMe(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token");
            }

            String token = authHeader.substring(7);
           UserResponse user = userService.getUserResponse(token);

            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request,HttpServletResponse response) {
        userService.logout(request, response);
        return ResponseEntity.ok("Đăng xuất thành công");
    }






}