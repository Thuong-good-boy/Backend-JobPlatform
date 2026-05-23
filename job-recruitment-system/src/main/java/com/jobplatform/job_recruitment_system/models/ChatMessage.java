package com.jobplatform.job_recruitment_system.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jobplatform.job_recruitment_system.enums.MessageType;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom room;

    @Column(name = "sender_id", nullable = false)
    @JsonProperty("senderId")
    private Long senderId;
    @Column(name = "is_read", columnDefinition = "boolean default false")
    private boolean isRead;
    @Column(columnDefinition = "TEXT")
    private String content;
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType = MessageType.TEXT;
    @Column(name = "file_url", columnDefinition = "TEXT")
    private String fileUrl;

    @Column(name = "file_name")
    private String fileName;
    @Column(name = "sent_at")
    @JsonProperty("sentAt")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime sentAt;
    @PrePersist
    protected void onCreate() {
        this.sentAt = LocalDateTime.now();
    }
}