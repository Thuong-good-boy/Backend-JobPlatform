package com.jobplatform.job_recruitment_system.models;

import com.jobplatform.job_recruitment_system.enums.PackageStatus;
import com.jobplatform.job_recruitment_system.enums.PackageType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "packages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Package {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;
     private String name;
     @Enumerated(EnumType.STRING)
     private PackageType type;
     private BigDecimal price;
     @Column(name="apply_limit")
     private Integer applyLimit;
     @Column(name="duration_days")
    private  Integer durationDays;
     @Column(name = "job_post_limit")
     private  Integer jobPostLimit;
     @Enumerated(EnumType.STRING)
    private PackageStatus status;
     @CreationTimestamp
     @Column(name = "created_at", updatable = false)
     private LocalDateTime createAt;
     @Column(name = "cv_view_limit")
     private Integer cvViewLimit;
        @Column(name = "points_granted")
        private Integer pointsGranted = 0;

}
