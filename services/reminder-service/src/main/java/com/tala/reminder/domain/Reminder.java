package com.tala.reminder.domain;

import com.tala.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Reminder entity for heads-up notifications
 */
@Entity
@Table(
    name = "reminders",
    schema = "reminders",
    indexes = {
        @Index(name = "idx_user_profile", columnList = "user_id, profile_id"),
        @Index(name = "idx_due_at", columnList = "due_at"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_category", columnList = "category")
    }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Reminder extends BaseEntity {
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "profile_id")
    private Long profileId;
    
    @Column(name = "source_event_id")
    private Long sourceEventId;
    
    @Column(name = "category", nullable = false, length = 50)
    private String category;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "due_at", nullable = false)
    private Instant dueAt;
    
    @Column(name = "valid_until")
    private Instant validUntil;
    
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";
    
    @Column(name = "snooze_until")
    private Instant snoozeUntil;
    
    @Column(name = "recurrence_rule")
    private String recurrenceRule;
    
    @Column(name = "priority", length = 20)
    @Builder.Default
    private String priority = "medium";
    
    @Column(name = "completed_at")
    private Instant completedAt;
    
    public void complete() {
        this.status = "COMPLETED";
        this.completedAt = Instant.now();
    }
    
    public void snooze(Instant until) {
        this.status = "SNOOZED";
        this.snoozeUntil = until;
    }
    
    public void cancel() {
        this.status = "CANCELED";
    }
    
    public void activate() {
        this.status = "ACTIVE";
        this.snoozeUntil = null;
    }
}
