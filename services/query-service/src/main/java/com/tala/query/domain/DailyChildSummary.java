package com.tala.query.domain;

import com.tala.core.domain.BaseEntity;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Type;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Daily child summary for aggregated metrics
 */
@Entity
@Table(
    name = "daily_child_summaries",
    schema = "analytics",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_profile_date", columnNames = {"profile_id", "date"})
    },
    indexes = {
        @Index(name = "idx_profile_date", columnList = "profile_id, date")
    }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DailyChildSummary extends BaseEntity {
    
    @Column(name = "profile_id", nullable = false)
    private Long profileId;
    
    @Column(name = "date", nullable = false)
    private LocalDate date;
    
    @Type(JsonBinaryType.class)
    @Column(name = "events_summary", columnDefinition = "jsonb")
    private Map<String, Object> eventsSummary;
    
    @Type(JsonBinaryType.class)
    @Column(name = "metrics", columnDefinition = "jsonb")
    private Map<String, Object> metrics;
    
    @Type(JsonBinaryType.class)
    @Column(name = "candidate_media_ids", columnDefinition = "jsonb")
    private List<Long> candidateMediaIds;
    
    @Type(JsonBinaryType.class)
    @Column(name = "candidate_incident_ids", columnDefinition = "jsonb")
    private List<Long> candidateIncidentIds;
    
    @Column(name = "total_events")
    private Integer totalEvents;
    
    @Column(name = "has_incident")
    private Boolean hasIncident;
    
    @Column(name = "has_sickness")
    private Boolean hasSickness;
}
