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

    @CreationTimestamp
    private LocalDateTime appliedAt;

    @Column(name = "fullname", length = 100)
    private String fullName;

}