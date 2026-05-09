package com.jobplatform.job_recruitment_system.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // Nhớ import
import com.jobplatform.job_recruitment_system.enums.AppStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
@Entity
@Table(name = "applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    @JsonIgnoreProperties({"applications", "company"})
    private Job job;

    @ManyToOne
    @JoinColumn(name = "cv_id")
    @JsonIgnoreProperties({"user", "cvData"})
    private Cv cv;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private AppStatus status = AppStatus.APPLIED;

    @Column(columnDefinition = "TEXT")
    private String coverLetter;

    @CreationTimestamp
    private LocalDateTime appliedAt;

    @Column(length = 100)
    private String fullname;

    @Column(length = 15)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String address;
}