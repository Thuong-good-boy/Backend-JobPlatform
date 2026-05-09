package com.jobplatform.job_recruitment_system.controllers;

import com.jobplatform.job_recruitment_system.services.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {
    private  final PaymentService paymentService;
    @PostMapping("/create-vnpay")
    public ResponseEntity<?> createPayment(@RequestBody Map<String,Object> payload, HttpServletRequest request){
        Long packageId = ((Number) payload.get("packageId")).longValue();
        Long amount = ((Number) payload.get("amount")).longValue();
        String  backCode = (String) payload.get("backCode");
        String paymentUrl = paymentService.createVnPayPaymentUrl(request,amount,packageId,backCode);
        return  ResponseEntity.ok(Map.of("paymentUrl",paymentUrl));
    }
    @GetMapping("/vnpay-return")
    public  void vnpayReturn(HttpServletRequest request, HttpServletResponse response){
        paymentService.vnPayReturn(request,response);

    }
    @GetMapping("/vnpay-ipn")
    public ResponseEntity<?> vnpayIpn(HttpServletRequest request){
        System.out.println("có vào vnpay-ipn");
        return  paymentService.vnpayIpn(request);
    }
}
