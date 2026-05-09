package com.jobplatform.job_recruitment_system.config;

import com.jobplatform.job_recruitment_system.enums.Role;
import com.jobplatform.job_recruitment_system.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull; // Import thêm cái này cho chuẩn
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain ) throws ServletException, IOException {
        String path = request.getServletPath();
        if (path.contains("/api/auth/login") ||
                path.contains("/api/auth/refresh") ||
                path.contains("/api/auth/register")||
                path.contains("/api/auth/verify") ||
                path.contains("/api/auth/forgot-password-verify") ||
                path.contains("/api/auth/forgot-password") ||
                path.contains("/api/auth/reset-password") ||
                path.contains("/api/auth/login-google")||
                path.contains("/api/auth/resendRegister-otp")||
                path.contains("/api/payment/vnpay-return") ||
                path.contains("/api/payment/vnpay_ipn")

        ) {

            filterChain.doFilter(request, response);
            return;
        }
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

//
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            filterChain.doFilter(request, response);
//            return;
//        }


        try {
            jwt = authHeader.substring(7);
            userEmail = jwtService.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails.getUsername())) {
                    String roleString = jwtService.extractRole(jwt);
                    boolean isPro = jwtService.extractPro(jwt);
                    Long userId= jwtService.extractId(jwt);
                    CustomUserDetails customUserDetails = new CustomUserDetails(
                            userId,
                            userEmail,
                            isPro,
                            Role.valueOf(roleString)
                    );
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            customUserDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"error\": \"Access Token đã hết hạn hoặc không hợp lệ!\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}