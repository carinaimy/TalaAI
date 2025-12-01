package com.tala.origindata.domain;

import com.tala.core.domain.BaseEntity;
import com.tala.origindata.constant.HealthReportType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Health Report Header
 * 
 * Medical visits, checkups, vaccinations
 */
@Entity
@Table(name = "health_reports", schema = "origin_data", indexes = {
    @Index(name = "idx_health_report_original_event", columnList = "original_event_id"),
    @Index(name = "idx_health_report_profile", columnList = "profile_id"),
    @Index(name = "idx_health_report_type", columnList = "report_type"),
    @Index(name = "idx_health_report_visit_time", columnList = "visit_time")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class HealthReport extends BaseEntity {
    
    @Column(name = "original_event_id", nullable = false, unique = true)
    private Long originalEventId;
    
    @Column(name = "profile_id", nullable = false)
    private Long profileId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 50)
    private HealthReportType reportType;
    
    @Column(name = "visit_time", nullable = false)
    private Instant visitTime;
    
    @Column(name = "provider_name", length = 255)
    private String providerName;
    
    @Column(name = "facility_name", length = 255)
    private String facilityName;
    
    @Column(name = "diagnosis", columnDefinition = "TEXT")
    private String diagnosis;
    
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;
    
    @Column(name = "next_appointment")
    private Instant nextAppointment;
    
    @OneToMany(mappedBy = "healthReport", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<HealthMeasurement> measurements = new ArrayList<>();
    
    @OneToMany(mappedBy = "healthReport", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<HealthMedication> medications = new ArrayList<>();
    
    @OneToMany(mappedBy = "healthReport", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<HealthVaccination> vaccinations = new ArrayList<>();
}
