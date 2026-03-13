package com.jobplatform.job_recruitment_system.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    @Value("${jwt.secret}")
     private  String secretKey;

    @Value("${jwt.expiration}")
    private  long jwtExpriration;

    public  String generateToken(String email){
        Map<String,Object> claims= new HashMap<>();
        return createToken(claims,email);
    }
    //Tạo ra một chuỗi JWT hoàn chỉnh, có chữ ký bảo mật, đại diện cho người dùng đã đăng nhập.
    private  String createToken(Map<String,Object> claims,String subject){
            return Jwts.builder().setClaims(claims).setSubject(subject)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + jwtExpriration))
                    .signWith(getSignKey(), SignatureAlgorithm.HS256).compact();

    }



    public    String extractUsername (String token){
        return  extractClaim(token, Claims::getSubject);
    }

    public  boolean isTokenValid(String token, String username){
            final String extractedUsername = extractUsername(token);
            return  (extractedUsername.equals(username) && !isTokenExpired(token));
    }



    private  <T> T extractClaim(String token, Function<Claims,T> claimsResolver){
        final  Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    // giải mã token
    private  Claims extractAllClaims(String token){
        return  Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    // kiểm tra hết hạn
    private boolean isTokenExpired(String token){
        return  extractExpiration(token).before(new Date());
    }

    private  Date extractExpiration (String token){
        return  extractClaim(token, Claims::getExpiration);
    }
    //    getSignKey() chuyển chuỗi secret dạng Base64 từ cấu hình thành một Key chuẩn,
    //    đủ mạnh để ký JWT bằng thuật toán HS256 theo yêu cầu của thư viện JJWT.
    private Key getSignKey(){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
