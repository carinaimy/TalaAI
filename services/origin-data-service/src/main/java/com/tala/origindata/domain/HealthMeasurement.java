package com.tala.origindata.domain;

import com.tala.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * Health Measurement
 * 
 * Physical measurements (height, weight, head circumference, etc.)
 */
@Entity
@Table(name = "health_measurements", schema = "origin_data", indexes = {
    @Index(name = "idx_health_measurement_report", columnList = "health_report_id"),
    @Index(name = "idx_health_measurement_type", columnList = "measurement_type")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class HealthMeasurement extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_report_id", nullable = false)
    private HealthReport healthReport;
    
    @Column(name = "measurement_type", nullable = false, length = 100)
    private String measurementType;
    
    @Column(name = "value", precision = 10, scale = 2)
    private BigDecimal value;
    
    @Column(name = "unit", length = 50)
    private String unit;
    
    @Column(name = "percentile", precision = 5, scale = 2)
    private BigDecimal percentile;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
