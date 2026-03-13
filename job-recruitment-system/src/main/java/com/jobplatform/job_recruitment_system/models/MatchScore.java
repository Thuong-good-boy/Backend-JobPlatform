package com.jobplatform.job_recruitment_system.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "match_scores")
@Data
public class MatchScore {
    @EmbeddedId
    private MatchScoreId id = new MatchScoreId();

    @ManyToOne
    @MapsId("jobId")
    @JoinColumn(name = "job_id")
    private Job job;

    @ManyToOne
    @MapsId("cvId")
    @JoinColumn(name = "cv_id")
    private Cv cv;

    private Double score;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "match_details",columnDefinition = "jsonb")
    private String match_details;

    private LocalDateTime calculated_at = LocalDateTime.now();
}


