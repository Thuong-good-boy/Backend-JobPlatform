package com.jobplatform.job_recruitment_system.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    @Autowired
    private StringRedisTemplate redisTemplate;



    public void saveData(String key, String value, long timeoutInMinutes) {
        redisTemplate.opsForValue().set(key, value, timeoutInMinutes, TimeUnit.MINUTES);
    }

    public String getData(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

}