package com.tala.origindata.domain;

import com.tala.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Day Care Report Header
 * 
 * Represents a daily report from daycare facility
 */
@Entity
@Table(name = "daycare_reports", schema = "origin_data", indexes = {
    @Index(name = "idx_daycare_report_original_event", columnList = "original_event_id"),
    @Index(name = "idx_daycare_report_profile_date", columnList = "profile_id,report_date")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DayCareReport extends BaseEntity {
    
    @Column(name = "original_event_id", nullable = false, unique = true)
    private Long originalEventId;
    
    @Column(name = "profile_id", nullable = false)
    private Long profileId;
    
    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;
    
    @Column(name = "daycare_name", length = 255)
    private String daycareName;
    
    @Column(name = "teacher_name", length = 255)
    private String teacherName;
    
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;
    
    @OneToMany(mappedBy = "dayCareReport", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DayCareReportItem> items = new ArrayList<>();
}
