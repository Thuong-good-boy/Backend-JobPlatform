package com.jobplatform.job_recruitment_system.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Data
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nối với bảng ChatRoom ông đã có
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom room;

    // ID của người gửi (có thể là ứng viên hoặc ID của user thuộc công ty)
    @Column(name = "sender_id", nullable = false)
    @JsonProperty("senderId")
    private Long senderId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "sent_at")
    @JsonProperty("sentAt")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS") // 🟢 THÊM DÒNG NÀY
    private LocalDateTime sentAt;
    @PrePersist
    protected void onCreate() {
        this.sentAt = LocalDateTime.now();
    }
}