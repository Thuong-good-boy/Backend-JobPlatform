package com.jobplatform.job_recruitment_system.config;

import com.jobplatform.job_recruitment_system.enums.Role;
import com.jobplatform.job_recruitment_system.exceptions.AppException;
import com.jobplatform.job_recruitment_system.exceptions.ErrorCode;
import com.jobplatform.job_recruitment_system.services.JwtService;
import com.jobplatform.job_recruitment_system.services.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {


    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    @Autowired
    @Lazy
    private final UserService userService;


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
                path.contains("/api/payment/vnpay_ipn") ||
                path.contains("/ws")

        ) {

            filterChain.doFilter(request, response);
            return;
        }
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;
        final Long userId;
        try {

            jwt = authHeader.substring(7);
            userEmail = jwtService.extractUsername(jwt);
            userId = jwtService.extractId(jwt);

            if (!userService.isActive(userId)) {
                throw new AppException(ErrorCode.AUTH_011);
            }

            if (userEmail != null &&
                    SecurityContextHolder.getContext()
                            .getAuthentication() == null) {

                UserDetails userDetails =
                        this.userDetailsService
                                .loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(
                        jwt,
                        userDetails.getUsername()
                )) {

                    String roleString =
                            jwtService.extractRole(jwt);

                    boolean isPro =
                            jwtService.extractPro(jwt);

                    CustomUserDetails customUserDetails =
                            new CustomUserDetails(
                                    userId,
                                    userEmail,
                                    isPro,
                                    Role.valueOf(roleString)
                            );

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    customUserDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authToken);
                }
            }

        } catch (AppException e) {

            response.setStatus(
                    HttpServletResponse.SC_FORBIDDEN
            );

            response.setContentType(
                    "application/json;charset=UTF-8"
            );

            response.getWriter().write(
                    "{\"error\":\""
                            + e.getErrorCode().getMessage()
                            + "\"}"
            );

            return;

        } catch (Exception e) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            response.setContentType(
                    "application/json;charset=UTF-8"
            );

            response.getWriter().write(
                    "{\"error\":"
                            + "\"Access Token đã hết hạn hoặc không hợp lệ!\"}"
            );

            return;
        }

        filterChain.doFilter(request, response);
    }
}