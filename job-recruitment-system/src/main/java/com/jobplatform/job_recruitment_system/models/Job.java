package com.jobplatform.job_recruitment_system.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jobplatform.job_recruitment_system.enums.JobStatus;
import jakarta.persistence.*;
import lombok.*; // Dùng Getter, Setter thay vì Data để an toàn hơn với JPA
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    private BigDecimal salaryMin;
    private BigDecimal salaryMax;

    private String location;

    @Enumerated(EnumType.STRING)
    private JobStatus status = JobStatus.PENDING;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_pinning")
    private Boolean isTrending;

    @Column(name = "pinning_until")
    private LocalDateTime trendingUntil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "job_skills",
            joinColumns = @JoinColumn(name = "job_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> skills = new HashSet<>();
}