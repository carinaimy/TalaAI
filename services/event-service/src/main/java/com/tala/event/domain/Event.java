package com.tala.event.domain;

import com.tala.core.domain.BaseEntity;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Universal Event entity
 * 
 * Stores all event types in a single table with JSONB for flexibility
 */
@Entity
@Table(
    name = "events",
    schema = "events",
    indexes = {
        @Index(name = "idx_profile_time", columnList = "profile_id, event_time"),
        @Index(name = "idx_event_type", columnList = "event_type"),
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_deleted_at", columnList = "deleted_at")
    }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Event extends BaseEntity {
    
    @Column(name = "profile_id", nullable = false)
    private Long profileId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;
    
    @Column(name = "event_time", nullable = false)
    private Instant eventTime;
    
    @Type(JsonBinaryType.class)
    @Column(name = "event_data", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> eventData;
    
    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;
    
    @Type(JsonBinaryType.class)
    @Column(name = "ai_tags", columnDefinition = "jsonb")
    private List<String> aiTags;
    
    @Column(name = "source", length = 50)
    @Builder.Default
    private String source = "USER_INPUT";
}
