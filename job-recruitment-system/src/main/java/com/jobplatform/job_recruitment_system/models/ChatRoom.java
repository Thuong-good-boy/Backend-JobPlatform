package com.jobplatform.job_recruitment_system.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Liên kết với Job (để biết họ chat về vị trí nào)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    // Liên kết với Công ty
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    // Liên kết với Ứng viên (User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}