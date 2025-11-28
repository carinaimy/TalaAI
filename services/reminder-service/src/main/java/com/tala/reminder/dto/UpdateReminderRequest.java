package com.tala.reminder.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Request DTO for updating reminders
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReminderRequest {
    
    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title;
    
    private String description;
    
    @Future(message = "Due date must be in the future")
    private Instant dueAt;
    
    private Instant validUntil;
    
    private String priority;
}
