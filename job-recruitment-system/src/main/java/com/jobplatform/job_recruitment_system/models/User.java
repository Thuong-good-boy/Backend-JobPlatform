package com.jobplatform.job_recruitment_system.models;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthoritiesContainer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.processing.Generated;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User  implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;
    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "user_role")
    private Role role;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "verification_code")
    private String verificationCode;

    @Column(name = "verification_expiration")
    private LocalDateTime verificationExpiration;

    @Column(name = "is_active")
    private boolean isActive = false;

    @Column(name = "auth_provider")
    private String authProvider;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        return  List.of( new SimpleGrantedAuthority(role.name()));
    }
    @Override
    public  String getUsername(){
        return  email;
    }
    // Kiểm tra tài khoản có hết hạn không.
    @Override
    public boolean isAccountNonExpired() { return true; }
    //Kiểm tra tài khoản có bị khóa không
    @Override
    public boolean isAccountNonLocked() { return true; }
    //Kiểm tra mật khẩu có hết hạn không.
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return isActive; }

}
