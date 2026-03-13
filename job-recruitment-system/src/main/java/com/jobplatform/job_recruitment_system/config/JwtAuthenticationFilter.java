package com.jobplatform.job_recruitment_system.config;

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
//    Authentication là object đại diện cho:
//
//    Trạng thái đăng nhập của người dùng.
//
//    Nó chứa:
//
//    principal (UserDetails hoặc username)
//
//    credentials (password hoặc null)
//
//    authorities (role)
//
//    authenticated (true/false)
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 1. Kiểm tra Header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Thêm TRY-CATCH để xử lý Token lỗi
        try {
            jwt = authHeader.substring(7);
            userEmail = jwtService.extractUsername(jwt); // <-- Dễ nổ lỗi ở đây nếu token hết hạn

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            //  QUAN TRỌNG: Nếu Token lỗi (hết hạn, sai format...), ta KHÔNG throw lỗi.
            // Ta chỉ log ra console để debug thôi.
            System.out.println("Lỗi xác thực Token: " + e.getMessage());

            // Request vẫn sẽ đi tiếp xuống dưới.
            // Nếu API là public (/api/jobs), nó vẫn sẽ được vào!
            // Nếu API là private, SecurityConfig sẽ chặn lại sau.
        }

        // 3. Cho phép Request đi tiếp
        filterChain.doFilter(request, response);
    }
}