package com.tala.reminder.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO for reminders
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReminderResponse {
    
    private Long id;
    private Long userId;
    private Long profileId;
    private Long sourceEventId;
    private String category;
    private String title;
    private String description;
    private Instant dueAt;
    private Instant validUntil;
    private String status;
    private Instant snoozeUntil;
    private String recurrenceRule;
    private String priority;
    private Instant completedAt;
    private Instant createdAt;
    private Instant updatedAt;
}
