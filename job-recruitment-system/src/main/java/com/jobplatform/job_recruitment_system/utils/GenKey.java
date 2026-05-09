package com.jobplatform.job_recruitment_system.utils;
import io.jsonwebtoken.security.Keys;
import java.util.Base64;

public class GenKey {
    public static void main(String[] args) {
        String key = Base64.getEncoder().encodeToString(Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256).getEncoded());
        System.out.println("mã là : "+key);
    }
}