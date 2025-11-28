package com.tala.event.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for events
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventResponse {
    
    private Long id;
    private Long profileId;
    private Long userId;
    private String eventType;
    private Instant eventTime;
    private Map<String, Object> eventData;
    private String aiSummary;
    private List<String> aiTags;
    private String source;
    private Instant createdAt;
    private Instant updatedAt;
}
