package com.jobplatform.job_recruitment_system.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "unlocked_cvs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnlockedCv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @Column(name = "unlocked_at", updatable = false)
    private LocalDateTime unlockedAt;
    @PrePersist
    protected void onCreate() {
        this.unlockedAt = LocalDateTime.now();
    }
}
