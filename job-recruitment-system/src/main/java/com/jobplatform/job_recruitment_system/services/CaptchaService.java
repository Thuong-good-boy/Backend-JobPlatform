package com.jobplatform.job_recruitment_system.services;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
@Service
public class CaptchaService {
    @Value("${google.recaptcha.secret}")
    private String recaptchaSecret;
    private static final String GOOGLE_RECAPTCHA_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";
    public boolean verifyCaptcha(String captchaToken){
        if (captchaToken == null || captchaToken.isEmpty()){
            return  false;
        }

        RestTemplate restTemplate = new RestTemplate();
        MultiValueMap<String,String> body= new LinkedMultiValueMap<>();
        body.add("secret",recaptchaSecret);
        body.add("response",captchaToken);
        try {
            Map<String, Object> response = restTemplate.postForObject(
             GOOGLE_RECAPTCHA_VERIFY_URL,body,Map.class
            );
            return  response != null && Boolean.TRUE.equals(response.get("success"));
        }catch (Exception e){
            return  false;
        }
    }
}
