package com.jobplatform.job_recruitment_system.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "cvs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"password", "jobs", "role", "authorities"}) // Tránh lộ info User
    private User user;

    @Column(name = "file_url")
    private String fileUrl; // Link file PDF

    // Tạm thời để String cho cột JSONB để tránh lỗi thư viện phức tạp lúc này
    // Sau này muốn xử lý JSON sâu hơn thì dùng thư viện hibernate-types sau
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "cv_data", columnDefinition = "jsonb")
    private String cvData;

    @CreationTimestamp
    private LocalDateTime createdAt;
}