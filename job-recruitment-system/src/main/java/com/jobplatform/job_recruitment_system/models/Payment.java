package com.jobplatform.job_recruitment_system.models;

import com.jobplatform.job_recruitment_system.enums.PackageStatus;
import com.jobplatform.job_recruitment_system.enums.PaymentMethod;
import com.jobplatform.job_recruitment_system.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne
    @JoinColumn(name = "package_id")
    private  Package jobPackage;
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    @Column(name = "amount")
    private  Long amount;
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    @Column(name = "transaction_ref")
    private  String transactionRef;
    @CreationTimestamp
    private LocalDateTime createdAt;
}
