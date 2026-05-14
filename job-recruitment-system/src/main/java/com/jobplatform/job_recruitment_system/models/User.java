    package com.jobplatform.job_recruitment_system.models;

    import com.fasterxml.jackson.annotation.JsonIgnore;
    import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
    import com.fasterxml.jackson.annotation.JsonProperty;
    import com.jobplatform.job_recruitment_system.enums.Role;
    import jakarta.persistence.*;
    import lombok.AllArgsConstructor;
    import lombok.Data;
    import lombok.NoArgsConstructor;
    import org.springframework.security.core.GrantedAuthority;
    import org.springframework.security.core.authority.SimpleGrantedAuthority;
    import org.springframework.security.core.userdetails.UserDetails;

    import java.time.LocalDateTime;
    import java.util.Collection;
    import java.util.List;

    @Entity
    @Table(name = "users")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class User implements UserDetails {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(unique = true, nullable = false)
        private String email;

        @Column(nullable = false)
        private String password;

        @JsonProperty("fullName")
        @Column(name = "full_name", nullable = false)
        private String fullName;

        @Enumerated(EnumType.STRING)
        private Role role;

        @Column(name = "created_at")
        private LocalDateTime createdAt;

        @Column(name = "active")
        private  Boolean active;

        @Column(name = "auth_provider")
        private String authProvider;

        @Column(name = "avatarUrl")
        private String avatarUrl;

        @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
        @com.fasterxml.jackson.annotation.JsonIgnore
        private List<RefreshToken> refreshTokens;


        @PrePersist
        protected void onCreate() {
            createdAt = LocalDateTime.now();
        }
        @JsonIgnore
        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return List.of(new SimpleGrantedAuthority(role.name()));
        }

        @JsonIgnore
        @Override
        public String getUsername() { return email; }

        @JsonIgnore
        @Override public boolean isAccountNonExpired() { return true; }

        @JsonIgnore
        @Override public boolean isAccountNonLocked() { return true; }

        @JsonIgnore
        @Override public boolean isCredentialsNonExpired() { return true; }

        @JsonIgnore
        @Override public boolean isEnabled() { return true; }
    }