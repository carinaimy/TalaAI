package com.tala.event.dto;

import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Request DTO for updating events
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventRequest {
    
    @PastOrPresent(message = "Event time cannot be in the future")
    private Instant eventTime;
    
    @Size(min = 1, message = "Event data cannot be empty if provided")
    private Map<String, Object> eventData;
    
    private String aiSummary;

    private String priority;

    private Integer urgencyHours;

    private String riskLevel;
}
