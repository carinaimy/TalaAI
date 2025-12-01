package com.tala.origindata.domain;

import com.tala.core.domain.BaseEntity;
import com.tala.origindata.constant.IncidentSeverity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Incident Report
 * 
 * Records incidents that occurred at daycare or elsewhere
 */
@Entity
@Table(name = "incident_reports", schema = "origin_data", indexes = {
    @Index(name = "idx_incident_report_original_event", columnList = "original_event_id"),
    @Index(name = "idx_incident_report_profile", columnList = "profile_id"),
    @Index(name = "idx_incident_report_severity", columnList = "severity"),
    @Index(name = "idx_incident_report_time", columnList = "incident_time")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentReport extends BaseEntity {
    
    @Column(name = "original_event_id", nullable = false, unique = true)
    private Long originalEventId;
    
    @Column(name = "profile_id", nullable = false)
    private Long profileId;
    
    @Column(name = "incident_time", nullable = false)
    private Instant incidentTime;
    
    @Column(name = "title", length = 255)
    private String title;
    
    @Column(name = "story", columnDefinition = "TEXT")
    private String story;
    
    @Column(name = "involved_people", columnDefinition = "TEXT")
    private String involvedPeople;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", length = 50)
    private IncidentSeverity severity;
    
    @Column(name = "handling_action", columnDefinition = "TEXT")
    private String handlingAction;
    
    @Column(name = "result", columnDefinition = "TEXT")
    private String result;
    
    @Column(name = "location", length = 255)
    private String location;
    
    @Column(name = "reported_by", length = 255)
    private String reportedBy;
}
