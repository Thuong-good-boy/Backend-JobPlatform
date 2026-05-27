package com.jobplatform.job_recruitment_system.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;

@Entity
@Table(name = "candidates")
@Data
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnore
    private User user;
    @Column(name = "title")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "experience_years")
    private Integer experienceYears = 0;

    private String location;

    @Column(name = "expected_salary_min")
    private Integer expectedSalaryMin;

    @Column(name = "expected_salary_max")
    private Integer expectedSalaryMax;

    @Column(name = "is_public")
    private Boolean isPublic = false;
    @Column(name = "ai_points")
    private Integer aiPoints = 0;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @JsonProperty("fullName")
    public String getFullName() {
        return this.user != null ? this.user.getFullName() : null;
    }
}
