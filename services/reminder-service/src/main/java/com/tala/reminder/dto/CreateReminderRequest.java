package com.tala.reminder.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Request DTO for creating reminders
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReminderRequest {
    
    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    private Long userId;
    
    private Long profileId;
    
    private Long sourceEventId;
    
    @NotBlank(message = "Category is required")
    @Size(max = 50, message = "Category must be less than 50 characters")
    private String category;
    
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title;
    
    private String description;
    
    @NotNull(message = "Due date is required")
    @Future(message = "Due date must be in the future")
    private Instant dueAt;
    
    private Instant validUntil;
    
    private String recurrenceRule;
    
    private String priority;
}
