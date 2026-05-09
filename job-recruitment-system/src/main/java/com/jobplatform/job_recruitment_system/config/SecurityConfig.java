package com.jobplatform.job_recruitment_system.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
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
                        .requestMatchers("/api/auth/**", "/error").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/jobs", "/api/jobs/**").hasAnyAuthority("CANDIDATE","COMPANY","ADMIN")
                        .requestMatchers("/api/admin/ai/sync-legacy-data").permitAll()
                        .requestMatchers(HttpMethod.POST,"/api/jobs/*/apply").hasAuthority("CANDIDATE")
                        .requestMatchers(HttpMethod.POST,"/api/jobs/*/save").hasAuthority("CANDIDATE")
                        .requestMatchers(HttpMethod.DELETE,"/api/jobs/*/unsave").hasAuthority("CANDIDATE")
                        .requestMatchers("/api/candidate/profile").hasAuthority("CANDIDATE")
                        .requestMatchers("/api/company/onboarding").hasAuthority("COMPANY")
                        .requestMatchers("/api/company/profile").hasAuthority("COMPANY")
                        .requestMatchers("/api/jobs/company/job").hasAuthority("CANDIDATE")
                        .requestMatchers(HttpMethod.POST,"/api/company/verify-license").hasAuthority("COMPANY")
                        .requestMatchers(HttpMethod.GET,"/api/company/profile/*").hasAuthority("COMPANY")
                        .requestMatchers(HttpMethod.GET,"/api/company/top-hiring").hasAuthority("CANDIDATE")
                        .requestMatchers(HttpMethod.GET,"/api/company/search").hasAuthority("CANDIDATE")
                        .requestMatchers(HttpMethod.PATCH,"/api/jobs/*/status").hasAnyAuthority("COMPANY","ADMIN")
                        .requestMatchers("/api/jobs/*").hasAnyAuthority("COMPANY","ADMIN")
                        .requestMatchers(HttpMethod.DELETE,"/api/jobs/*").hasAuthority("COMPANY")
                        .requestMatchers(HttpMethod.POST,"/api/application/apply").hasAuthority("CANDIDATE")
                        .requestMatchers(HttpMethod.GET, "/api/applications/job/**").hasAuthority("COMPANY")
                        .requestMatchers("/api/Skill").hasAnyAuthority("COMPANY","ADMIN")
                        .requestMatchers("/api/company/profile").hasAuthority("COMPANY")
                        .requestMatchers("/api/jobs/create").hasAuthority("COMPANY")
                        .requestMatchers("/api/package").hasAnyAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET,"/api/admin/Summary").hasAnyAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET,"/api/admin/UserManagement").hasAnyAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/Summary/7ngay").hasAnyAuthority("ADMIN", "COMPANY")
                        .requestMatchers(HttpMethod.GET,"/api/Summary/list_last").hasAnyAuthority("ADMIN","COMPANY")
                        .requestMatchers(HttpMethod.GET,"/api/package/cadidate").hasAnyAuthority("CANDIDATE")
                        .requestMatchers(HttpMethod.GET,"/api/package/company").hasAnyAuthority("COMPANY")
                        .requestMatchers(HttpMethod.POST,"/api/payment/create-vnpay").hasAnyAuthority("CANDIDATE","COMPANY")
                        .requestMatchers(HttpMethod.GET,"/api/payment/vnpay-return").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/payment/vnpay_ipn").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/candidate/company/job").hasAuthority("CANDIDATE")
                        .requestMatchers(HttpMethod.GET,"/api/reportreason").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET,"/api/company/candidate/search").hasAuthority("COMPANY")
                        .requestMatchers(HttpMethod.GET,"/api/company/candidate/profile").hasAuthority("COMPANY")
                        .requestMatchers(HttpMethod.POST,"/api/report").hasAnyAuthority("CANDIDATE","COMPANY")
                        .requestMatchers(HttpMethod.GET,"/api/report").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT,"/apt/report/*/process").hasAuthority("ADMIN")
                        .requestMatchers("/ws/**").permitAll()
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
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}