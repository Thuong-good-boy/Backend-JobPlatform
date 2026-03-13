package com.jobplatform.job_recruitment_system.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // 1. Cho phép các API Public (Auth & Error)
                        .requestMatchers("/api/auth/**", "/error").permitAll()

                        // 2. Cho phép xem danh sách Job (GET)
                        // Lưu ý: Khai báo cả "/api/jobs" và "/api/jobs/**" để chắc chắn
                        .requestMatchers(HttpMethod.GET, "/api/jobs", "/api/jobs/**").permitAll()

                        .requestMatchers(HttpMethod.POST,"/api/jobs/*/apply").hasAuthority("CANDIDATE")
                        .requestMatchers(HttpMethod.POST,"/api/jobs/*/save").hasAuthority("CANDIDATE")
                        .requestMatchers(HttpMethod.DELETE,"/api/jobs/*/unsave").hasAuthority("CANDIDATE")
                        .requestMatchers(HttpMethod.POST,"/api/jobs/{jobId}/apply").hasAuthority("CANDIDATE")
                        .requestMatchers(HttpMethod.POST, "/api/jobs/**").hasAuthority("COMPANY")
                        .requestMatchers(HttpMethod.PUT, "/api/jobs/**").hasAuthority("COMPANY")
                        .requestMatchers(HttpMethod.DELETE, "/api/jobs/**").hasAuthority("COMPANY")
                        // aply cv
                        .requestMatchers("/api/applications/**").hasAuthority("CANDIDATE")
                        // 4. Các request còn lại phải đăng nhập
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}