package com.tala.event.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Request DTO for creating events
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEventRequest {
    
    @NotNull(message = "Profile ID is required")
    @Positive(message = "Profile ID must be positive")
    private Long profileId;
    
    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    private Long userId;
    
    @NotBlank(message = "Event type is required")
    @Size(max = 50, message = "Event type must be less than 50 characters")
    private String eventType;
    
    @NotNull(message = "Event time is required")
    @PastOrPresent(message = "Event time cannot be in the future")
    private Instant eventTime;
    
    @NotNull(message = "Event data is required")
    @Size(min = 1, message = "Event data cannot be empty")
    private Map<String, Object> eventData;
    
    private String aiSummary;
    
    @Size(max = 50, message = "Source must be less than 50 characters")
    private String source;
}
