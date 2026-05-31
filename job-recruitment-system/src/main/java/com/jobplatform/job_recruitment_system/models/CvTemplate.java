package com.jobplatform.job_recruitment_system.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cv_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CvTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "template_key", nullable = false, unique = true)
    private String templateKey;

    @Column(name = "image_url", nullable = false, length = 512)
    private String imageUrl;

    @Column(name = "is_active")
    private boolean isActive = true;

    // CỘT MỚI: Số lượt người dùng đã chọn mẫu này
    @Column(name = "usage_count")
    private int usageCount = 0;
}