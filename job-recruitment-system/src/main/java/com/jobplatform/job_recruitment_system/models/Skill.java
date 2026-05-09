package com.jobplatform.job_recruitment_system.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "skills")
@Data
@NoArgsConstructor
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "skill_name", unique = true, nullable = false)
    private String skillName;
    public Skill(String skillName) {
        this.skillName = skillName;
    }
}