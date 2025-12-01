package com.tala.origindata.domain;

import com.tala.core.domain.BaseEntity;
import com.tala.origindata.constant.DayCareReportType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Type;

import java.time.Instant;

/**
 * Day Care Report Item Detail
 * 
 * Individual activities/events within a daycare report
 */
@Entity
@Table(name = "daycare_report_items", schema = "origin_data", indexes = {
    @Index(name = "idx_daycare_item_report", columnList = "daycare_report_id"),
    @Index(name = "idx_daycare_item_type", columnList = "item_type"),
    @Index(name = "idx_daycare_item_time", columnList = "event_time")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DayCareReportItem extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daycare_report_id", nullable = false)
    private DayCareReport dayCareReport;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 50)
    private DayCareReportType itemType;
    
    @Column(name = "event_time")
    private Instant eventTime;
    
    @Column(name = "title", length = 255)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Type(JsonBinaryType.class)
    @Column(name = "details", columnDefinition = "jsonb")
    private String details;
}
