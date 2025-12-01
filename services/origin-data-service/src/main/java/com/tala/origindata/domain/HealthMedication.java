package com.tala.origindata.domain;

import com.tala.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Health Medication
 * 
 * Prescribed medications
 */
@Entity
@Table(name = "health_medications", schema = "origin_data", indexes = {
    @Index(name = "idx_health_medication_report", columnList = "health_report_id")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class HealthMedication extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_report_id", nullable = false)
    private HealthReport healthReport;
    
    @Column(name = "medication_name", nullable = false, length = 255)
    private String medicationName;
    
    @Column(name = "dosage", length = 100)
    private String dosage;
    
    @Column(name = "frequency", length = 100)
    private String frequency;
    
    @Column(name = "start_date")
    private Instant startDate;
    
    @Column(name = "end_date")
    private Instant endDate;
    
    @Column(name = "purpose", columnDefinition = "TEXT")
    private String purpose;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
