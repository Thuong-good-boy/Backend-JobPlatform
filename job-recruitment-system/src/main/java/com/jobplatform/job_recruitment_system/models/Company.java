package com.jobplatform.job_recruitment_system.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "companies")
@Data
public class Company {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    @JsonIgnore
    @ToString.Exclude
    private User user;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    private String website;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "tax_code", nullable = false)
    private String taxCode;

    @Column(name = "license_image_url")
    private String licenseImageUrl; // Link ảnh GPKD

    @Column(name = "is_verified")
    private boolean isVerified = false;

    private String logoUrl;
}