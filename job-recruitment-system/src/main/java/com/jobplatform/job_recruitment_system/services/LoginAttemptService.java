package com.jobplatform.job_recruitment_system.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService{
    private final RedisService redisService;

    @Value("${login.attempt.max}")
    private int maxAttempt;

    @Value("${login.attempt.lock-minutes}")
    private long lockTimeMinutes;

    public  LoginAttemptService(RedisService redisService){
        this.redisService = redisService;
    }
    public  void loginFailed(String isAddress){
        String key = "login_fail_id:" +isAddress;
        String currentAtttemptsStr= redisService.getData(key);
        int attempts =1;
        if(currentAtttemptsStr !=null){
            attempts = Integer.parseInt(currentAtttemptsStr) +1;
            System.out.println("so lan loi: "+ attempts);
        }
        redisService.saveData(key,String.valueOf(attempts),lockTimeMinutes);
    }
    public  void loginSucceeded(String ipAddress){
        String key = "login_fail_ip:" +ipAddress;
        redisService.delete(key);
    }
    public boolean isCaptchaRequired(String ipAddress){
        String key ="login_fail_id:"+ipAddress;
        String attemptsStr = redisService.getData(key);
        if(attemptsStr ==null) return  false;
        return Integer.parseInt(attemptsStr)>=maxAttempt;
    }
}
