package com.jobplatform.job_recruitment_system.services;

import com.jobplatform.job_recruitment_system.config.VNPayConfig;
import com.jobplatform.job_recruitment_system.enums.*;
import com.jobplatform.job_recruitment_system.exceptions.AppException;
import com.jobplatform.job_recruitment_system.exceptions.ErrorCode;
import com.jobplatform.job_recruitment_system.models.*;
import com.jobplatform.job_recruitment_system.models.Package;
import com.jobplatform.job_recruitment_system.repositories.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final VNPayConfig vnPayConfig;
    private  final PaymentRepository paymentRepository;
    private  final PackageRepository packageRepository;
    private  final UserService userService;
    private  final UserSubscriptionRepository userSubscriptionRepository;
    private  final CvRepository cvRepository;
    private  final  AiMatchingService aiMatchingService;
    private  final UserRepository userRepository;
    private  final  CompanyRepository companyRepository;
    private  final CandidateRepository candidateRepository;
    public String createVnPayPaymentUrl(HttpServletRequest request, Long amount, Long packageId,String bankCode) {
        String vnp_Version = vnPayConfig.vnp_Version;
        String vnp_Command = vnPayConfig.vnp_Command;
        String vnp_TxnRef = vnPayConfig.getRandomNumber(8);
        String vnp_IpAddr = vnPayConfig.getIpAddress(request);
        String vnp_TmnCode = vnPayConfig.vnp_TmnCode;

        long amountInVNPayFormat = (long) amount * 100;

        // 2. Gom các tham số vào một Map
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amountInVNPayFormat));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang: " + vnp_TxnRef + " cho goi ID: " + packageId);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        if (bankCode != null && !bankCode.isEmpty()) {
            vnp_Params.put("vnp_BankCode", bankCode);
        }
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);


        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        Payment payment = new Payment();
        payment.setTransactionRef(vnp_TxnRef);
        payment.setAmount(amount);
        payment.setStatus(PaymentStatus.PENDING);
        Package aPackage = packageRepository.findById(packageId).orElse(null);
        payment.setJobPackage(aPackage);
        Long userId = userService.getCurrentUserId();
        User user = userService.getUserId(userId).orElse(null);
        payment.setUser(user);
        payment.setPaymentMethod(PaymentMethod.VNPAY);

        paymentRepository.save(payment);

        // 5. Build chuỗi và Mã hóa (Thuật toán của VNPay)
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames); // BẮT BUỘC phải sắp xếp theo bảng chữ cái
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        // Tạo mã băm bằng HashSecret
        String queryUrl = query.toString();
        String vnp_SecureHash = vnPayConfig.hmacSHA512(vnPayConfig.secretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        return vnPayConfig.vnp_PayUrl + "?" + queryUrl;
    }
        public boolean verifyVnPayCallback(Map<String, String> fields) {
            String vnp_SecureHash = fields.get("vnp_SecureHash");

            fields.remove("vnp_SecureHashType");
            fields.remove("vnp_SecureHash");

            // Sắp xếp và nối chuỗi giống hệt lúc tạo link
            List<String> fieldNames = new ArrayList<>(fields.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            try {
                Iterator<String> itr = fieldNames.iterator();
                while (itr.hasNext()) {
                    String fieldName = itr.next();
                    String fieldValue = fields.get(fieldName);
                    if ((fieldValue != null) && (fieldValue.length() > 0)) {
                        hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                        if (itr.hasNext()) hashData.append('&');
                    }
                }
            } catch (Exception e) {
                return false;
            }

            String signValue = vnPayConfig.hmacSHA512(vnPayConfig.secretKey, hashData.toString());

            // So sánh 2 chữ ký
            return signValue.equals(vnp_SecureHash);
        }
        @Transactional
        public  void    vnPayReturn(HttpServletRequest request, HttpServletResponse response){
            Map<String, String> fields= new HashMap<>();
            for (Enumeration<String> params = request.getParameterNames();params.hasMoreElements();){
                String fieldName = params.nextElement();
                String fieldValue= request.getParameter(fieldName);
                if((fieldValue!=null)&& (fieldValue.length()>0)){
                    fields.put(fieldName,fieldValue);
                }
            }
            try {
                String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
                String vnp_TxnRef= request.getParameter("vnp_TxnRef");

                if (verifyVnPayCallback(fields)) {
                    if ("00".equals(vnp_ResponseCode)) {
                        Payment payment = paymentRepository.findByTransactionRef(vnp_TxnRef).orElseThrow(() -> new AppException(ErrorCode.PAYMENT_001));
                        payment.setStatus(PaymentStatus.SUCCESS);
                        payment.setPaidAt(LocalDateTime.now());
                        paymentRepository.save(payment);
                        Package aPackage= packageRepository.findById(payment.getJobPackage().getId()).orElse(null);
                        User user =payment.getUser();
                        if(aPackage!= null){
                            if(Role.COMPANY.equals(user.getRole())){
                                Company company = companyRepository.findByUser(user).orElse(null);
                                if(company==null){
                                    throw new AppException(ErrorCode.COM_001);
                                }
                                if(aPackage.getJobPostLimit()!= null && aPackage.getJobPostLimit()>0 ){
                                    int currentBoosts = company.getRemainingBoosts() ;
                                    company.setRemainingBoosts(currentBoosts+aPackage.getJobPostLimit());
                                    companyRepository.save(company);

                                }
                                if(PackageType.CV_UNLOCK.equals(aPackage.getType())&& aPackage.getCvViewLimit()>0){
                                    int currentCvViews = company.getRemainingCvViews();
                                    company.setRemainingCvViews(currentCvViews+ aPackage.getCvViewLimit());
                                    companyRepository.save(company);
                                }

                            }else{
                                Candidate candidate= candidateRepository.findByUserId(user.getId()).orElseThrow(()-> new AppException(ErrorCode.AUTH_008));
                                if(PackageType.AI_ASSISTANT.equals(aPackage.getType())&& aPackage.getPointsGranted()>0){
                                    int currentAiPoints = candidate.getAiPoints();
                                    candidate.setAiPoints(currentAiPoints+ aPackage.getPointsGranted());
                                    candidateRepository.save(candidate);
                                }
                            }

                            if(PackageType.COMPANY_PRO.equals(aPackage.getType())|| PackageType.CANDIDATE_PRO.equals(aPackage.getType())){
                                UserSubscription existingSub = userSubscriptionRepository.findActiveSubscription(user.getId());
                                boolean ispro = userSubscriptionRepository.userispro(existingSub.getUser().getId());
                                if (PackageType.CANDIDATE_PRO.equals(aPackage.getType()) && !ispro) {
                                    List<Cv> dsCv = cvRepository.findAllByUser_IdAndActiveTrueOrderByCreatedAtDesc(user.getId());
                                    for (Cv cv : dsCv) {
                                        aiMatchingService.processNewCv(cv);
                                    }
                                }
                                if(existingSub != null){
                                    System.out.println("ngày còn lại : " + existingSub.getEndDate() + "   ngày cộng vào : " + aPackage.getDurationDays());
                                    existingSub.setEndDate(existingSub.getEndDate().plusDays(aPackage.getDurationDays()));
                                    existingSub.setJobPackage(aPackage);
                                    userSubscriptionRepository.save(existingSub);

                                }else{
                                    UserSubscription newSub = new UserSubscription();
                                    newSub.setUser(user);
                                    newSub.setJobPackage(aPackage);
                                    newSub.setStatus(SubscriptionStatus.ACTIVE);
                                    newSub.setStartDate(LocalDateTime.now());
                                    newSub.setEndDate(LocalDateTime.now().plusDays(aPackage.getDurationDays()));
                                    newSub.setCreateAt(LocalDateTime.now());
                                    userSubscriptionRepository.save(newSub);
                                }

                            }


                        }
                        response.sendRedirect("https://pathuongdev.id.vn/payment-success");

                    } else {
                        response.sendRedirect("https://pathuongdev.id.vn/payment-failed?code=" + vnp_ResponseCode);
                    }
                } else {
                    response.sendRedirect("https://pathuongdev.id.vn/payment-error");
                }
            }catch (IOException e){
                e.printStackTrace();
            }

        }

        public ResponseEntity<?> vnpayIpn(HttpServletRequest request) {
            Map<String, String> fields = new HashMap<>();
            for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
                String fieldName = params.nextElement();
                String fieldValue = request.getParameter(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    fields.put(fieldName, fieldValue);
                }
            }

            if (verifyVnPayCallback(fields)) {
                String vnp_TxnRef = request.getParameter("vnp_TxnRef");
                String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
                Payment payment = paymentRepository.findByTransactionRef(vnp_TxnRef).orElseThrow(() -> new AppException(ErrorCode.PAYMENT_001));
                if ("00".equals(vnp_ResponseCode)) {
                    payment.setStatus(PaymentStatus.SUCCESS);
                    paymentRepository.save(payment);
                } else {
                    payment.setStatus(PaymentStatus.FAILED);
                    paymentRepository.save(payment);
                }
                return ResponseEntity.ok(Map.of("RspCode", "00", "Message", "Confirm Success"));

            } else {
                return ResponseEntity.ok(Map.of("RspCode", "97", "Message", "Invalid Checksum"));
            }

        }
}
