package com.tala.origindata.domain;

import com.tala.core.domain.BaseEntity;
import com.tala.origindata.constant.HomeEventType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Type;

import java.time.Instant;

/**
 * Home Event
 * 
 * Events recorded by parents at home
 */
@Entity
@Table(name = "home_events", schema = "origin_data", indexes = {
    @Index(name = "idx_home_event_original_event", columnList = "original_event_id"),
    @Index(name = "idx_home_event_profile", columnList = "profile_id"),
    @Index(name = "idx_home_event_type", columnList = "event_type"),
    @Index(name = "idx_home_event_time", columnList = "event_time")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class HomeEvent extends BaseEntity {
    
    @Column(name = "original_event_id", nullable = false, unique = true)
    private Long originalEventId;
    
    @Column(name = "profile_id", nullable = false)
    private Long profileId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private HomeEventType eventType;
    
    @Column(name = "event_time", nullable = false)
    private Instant eventTime;
    
    @Column(name = "title", length = 255)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Type(JsonBinaryType.class)
    @Column(name = "details", columnDefinition = "jsonb")
    private String details;
    
    @Column(name = "location", length = 255)
    private String location;
}
