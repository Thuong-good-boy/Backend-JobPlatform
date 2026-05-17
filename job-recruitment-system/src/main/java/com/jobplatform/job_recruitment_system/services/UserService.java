package com.jobplatform.job_recruitment_system.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.jobplatform.job_recruitment_system.config.CustomUserDetails;
import com.jobplatform.job_recruitment_system.dtos.Response.AdminUserResponse;
import com.jobplatform.job_recruitment_system.dtos.Response.LoginResponse;
import com.jobplatform.job_recruitment_system.dtos.request.*;
import com.jobplatform.job_recruitment_system.exceptions.AppException;
import com.jobplatform.job_recruitment_system.exceptions.ErrorCode;
import com.jobplatform.job_recruitment_system.mapper.CandidateMapper;
import com.jobplatform.job_recruitment_system.mapper.CompanyMapper;
import com.jobplatform.job_recruitment_system.models.Candidate;
import com.jobplatform.job_recruitment_system.models.Company;
import com.jobplatform.job_recruitment_system.models.RefreshToken;
import com.jobplatform.job_recruitment_system.enums.Role;
import com.jobplatform.job_recruitment_system.models.User;
import com.jobplatform.job_recruitment_system.repositories.CandidateRepository;
import com.jobplatform.job_recruitment_system.repositories.CompanyRepository;
import com.jobplatform.job_recruitment_system.repositories.UserRepository;
import com.jobplatform.job_recruitment_system.repositories.UserSubscriptionRepository;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.jobplatform.job_recruitment_system.mapper.UserMapper;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RedisService redisService;
    @Autowired
    @Lazy
    private  AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private  final UserSubscriptionRepository userSubscriptionRepository;
    private  final CandidateRepository candidateRepository;
    private  final CandidateMapper candidateMapper;
    @Value("${google.client.id}")
    private String googleClientId;
    private  final  CaptchaService captchaService;
    private  final  LoginAttemptService loginAttemptService;
    private  final CompanyRepository companyRepository;
    private  final CompanyMapper companyMapper;
    public LoginResponse login(LoginRequest userrequest, HttpServletRequest request) {
        String clientIp = getCLientIp(request);

        boolean requireCaptcha = loginAttemptService.isCaptchaRequired(clientIp);

        if (requireCaptcha && !userrequest.getRole().equals("ADMIN")) {

            if (userrequest.getCaptchaToken() == null || userrequest.getCaptchaToken().isEmpty() ) {
                throw new AppException(ErrorCode.AUTH_009);
            }

            boolean isValidCaptcha = captchaService.verifyCaptcha(userrequest.getCaptchaToken());
            if (!isValidCaptcha) {
                throw new AppException(ErrorCode.AUTH_010);
            }
        }
        User user = findByEmail(userrequest.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_005));
        if(!user.getActive()){
            throw  new AppException(ErrorCode.AUTH_011);
        }
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userrequest.getEmail(), userrequest.getPassword())
            );
        } catch (AuthenticationException e) {
            loginAttemptService.loginFailed(clientIp);

            if (loginAttemptService.isCaptchaRequired(clientIp)) {
                throw new AppException(ErrorCode.AUTH_009);
            }

            throw new AppException(ErrorCode.AUTH_005);
        }


        if (!user.getRole().toString().trim().equalsIgnoreCase(userrequest.getRole().trim())) {
            throw new AppException(ErrorCode.AUTH_006);
        }

        loginAttemptService.loginSucceeded(clientIp);

        LoginResponse response = userMapper.toLoginResponse(user);
        boolean isPro = userSubscriptionRepository.userispro(user.getId());
        response.setAccessToken(jwtService.generateAccessToken(user.getId(),userrequest.getEmail(),isPro,user.getRole()));
        response.setRefreshToken(refreshTokenService.createRefreshToken(userrequest.getEmail()).getToken());
        return response;
    }
    public String getCLientIp(HttpServletRequest request){
        String xfHeader = request.getHeader("X-Forwarded-For");
        if(xfHeader == null){
            return  request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
    public String refreshToken(String requestRefreshToken) {

        if (requestRefreshToken == null || requestRefreshToken.isBlank()) {
            throw new AppException(ErrorCode.AUTH_002);
        }
        RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken)
                .orElseThrow(() -> new AppException(ErrorCode.AUTH_002));

        refreshTokenService.verifyExpiration(refreshToken);

        User user = refreshToken.getUser();

        String newAccessToken = jwtService.generateAccessToken(user.getId(),user.getEmail(), userSubscriptionRepository.userispro(user.getId()),user.getRole() );

        return newAccessToken;
    }

    public void registerUser(RegisterRequest registerRequest) {
        if (userRepository.findByEmailAndActiveTrue(registerRequest.getEmail()).isPresent()) {
            throw new AppException(ErrorCode.USER_001);
        }

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        try {
            ObjectMapper mapper = new ObjectMapper();
            String userJson = mapper.writeValueAsString(registerRequest);


            redisService.saveData("REG_DATA:" + registerRequest.getEmail(), userJson, 5);
            redisService.saveData("REG_OTP:" + registerRequest.getEmail(), otp, 5);
        } catch (Exception e) {
            throw new AppException(ErrorCode.USER_011);
        }

        try {
            log.debug(otp);
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

        response.setAccessToken(jwtService.generateAccessToken(user.getId(),user.getEmail(), userSubscriptionRepository.userispro(user.getId()),user.getRole()));
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

            User user = userMapper.toEntity(request);

            // Cấu hình các trường bảo mật
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setAuthProvider("LOCAL");
            user.setActive(true);

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
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        return userDetails.getId() ;
    }
    public  Role getCurrentUserRode(){
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails)  authentication.getPrincipal();
        return  userDetails.getRole();
    }
    public  boolean getCurrentUserIsPro(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.isPro();
    }

    public void resendOtp(String email) {
        if (userRepository.findByEmailAndActiveTrue(email).isPresent()) {
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
    public void resendOtpRegister(String email) {
        if (userRepository.findByEmailAndActiveTrue(email).isPresent()) {
            throw new AppException(ErrorCode.USER_002);
        }

        String userJson = redisService.getData("REG_DATA:" + email);
        if (userJson == null) {
            throw new AppException(ErrorCode.USER_008);
        }

        String newOtp = String.valueOf(new Random().nextInt(900000) + 100000);

        try {
            redisService.saveData("REG_OTP:" + email, newOtp, 5);
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
        User user= userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_003));
        if(!user.getActive()){
            throw  new AppException(ErrorCode.AUTH_011);
        }
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

        User user = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_005));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        redisService.delete("RESET_TOKEN:" + request.getResetToken());
    }
    public Optional<User> findByEmail(String email){
        return userRepository.findByEmail(email);
    }

    public User getReferenceById(Long userId){
        return userRepository.getReferenceById(userId);
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

        User user = userRepository.findByEmailAndActiveTrue(email).orElse(null);
        System.out.println(request.getRole());
        boolean isNew = false;
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setFullName((String) payload.get("name"));
            user.setAuthProvider("GOOGLE");
            user.setRole(Role.valueOf(request.getRole()));
            user.setActive(true);
            user = userRepository.save(user);
            isNew = true;
        } else {
            if (!"GOOGLE".equals(user.getAuthProvider())) {
                user.setAuthProvider("GOOGLE");
                user = userRepository.save(user);
            }
        }
        LoginResponse response = userMapper.toLoginResponse(user);
        response.setAccessToken(jwtService.generateAccessToken(user.getId(),user.getEmail(),userSubscriptionRepository.userispro(user.getId()),user.getRole()));
        response.setRefreshToken(refreshTokenService.createRefreshToken(user.getEmail()).getToken());
        response.setNew(isNew);
        return response;
    }
    public  Optional<User> getUserId(Long userId){
        return  userRepository.findById(userId);
    }

    public Page<AdminUserResponse> getAdminUserResponse(Pageable page , Role role, String search){
        return userRepository.getAdminUserResponse(page,role,search);

    }
    public void blockUser(Long userId){
        userRepository.blockUser(userId);
    }
    public void unBlockUser(Long userId){

        userRepository.unBlockUser(userId);

    }
    public  Boolean isActive(Long id){
        return  userRepository.findActiveById(id);
    }
    @Transactional
    public void  updateCandidate(Long  id, CandidateUpdateRequest request){
        Candidate candidate = candidateRepository.findByUserId(id).orElseThrow(()-> new AppException(ErrorCode.AUTH_008));
        User user = getUserId(id).orElseThrow(()-> new AppException(ErrorCode.AUTH_008));
        user.setFullName(request.getFullName());
        userRepository.save(user);
        candidateMapper.upDateCandidateAdmin( request,candidate);
        candidateRepository.save(candidate);
    }
    @Transactional
    public  void updateCompany(Long id,CompanyUpdateRequest request){
        User  user = userRepository.findByCompanyId(id);
        user.setFullName(request.getFullName());
        userRepository.save(user);
        Company company = companyRepository.getReferenceById(id);
        companyMapper.updateCompany(request, company);
        companyRepository.save(company);
    }

}