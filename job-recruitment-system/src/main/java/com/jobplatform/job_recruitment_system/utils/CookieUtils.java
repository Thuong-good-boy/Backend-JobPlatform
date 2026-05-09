package com.jobplatform.job_recruitment_system.utils;

import org.springframework.http.ResponseCookie;

public class CookieUtils {
    public static ResponseCookie createRefreshTokenCookie(String refreshToken, boolean rememberMe) {
        long maxAge = rememberMe?7 * 24 * 60 * 60:24 * 60 * 60;

        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(maxAge)
                .sameSite("Lax")
                .build();
    }
    public static ResponseCookie clearCookie(String cookieName) {
        return ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .build();
    }
}
