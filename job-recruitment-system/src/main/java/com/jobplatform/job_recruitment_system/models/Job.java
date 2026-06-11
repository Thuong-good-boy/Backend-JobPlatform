package com.jobplatform.job_recruitment_system.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jobplatform.job_recruitment_system.enums.JobStatus;
import jakarta.persistence.*;
import lombok.*; // Dùng Getter, Setter thay vì Data để an toàn hơn với JPA
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.springframework.beans.factory.annotation.Autowired;

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
@Audited
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
    @NotAudited
    private JobStatus status = JobStatus.PENDING;

    @Column(name = "view_count", columnDefinition = "int default 0")
    @NotAudited
    private Integer viewCount = 0;

    @Column(name = "click_count", columnDefinition = "int default 0")
    @NotAudited
    private Integer clickCount = 0;
    @CreationTimestamp
    @Column(updatable = false)
                    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    @NotAudited
    private LocalDateTime updatedAt;

    @Column(name = "is_pinning")
    @NotAudited
    private Boolean isTrending;

    @Column(name = "pinning_until")
    @NotAudited
    private LocalDateTime trendingUntil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Company company;

    @Column(name = "last_boosted_at")
    @NotAudited
    private LocalDateTime lastBoostedAt;

    @Column(name ="weight_skill")
    private double weightSkill;

    @Column(name = "weight_experience")
    private double weightExperience;

    @Column(name = "weight_education")
    private double weightEducation;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "job_skills",
            joinColumns = @JoinColumn(name = "job_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Set<Skill> skills = new HashSet<>();
}