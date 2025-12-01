package com.tala.origindata.domain;

import com.tala.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Health Vaccination
 * 
 * Vaccination records
 */
@Entity
@Table(name = "health_vaccinations", schema = "origin_data", indexes = {
    @Index(name = "idx_health_vaccination_report", columnList = "health_report_id"),
    @Index(name = "idx_health_vaccination_name", columnList = "vaccine_name")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class HealthVaccination extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_report_id", nullable = false)
    private HealthReport healthReport;
    
    @Column(name = "vaccine_name", nullable = false, length = 255)
    private String vaccineName;
    
    @Column(name = "dose_number")
    private Integer doseNumber;
    
    @Column(name = "administered_date")
    private Instant administeredDate;
    
    @Column(name = "lot_number", length = 100)
    private String lotNumber;
    
    @Column(name = "next_dose_due")
    private Instant nextDoseDue;
    
    @Column(name = "reaction", columnDefinition = "TEXT")
    private String reaction;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
