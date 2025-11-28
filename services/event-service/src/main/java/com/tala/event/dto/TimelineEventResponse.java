package com.tala.event.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Timeline-specific event response with grouping support
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TimelineEventResponse {
    
    private Long id;
    private Long profileId;
    private String eventType;
    private Instant eventTime;
    private LocalDate eventDate;
    private String aiSummary;
    private List<String> aiTags;
    private String priority;
    private Integer urgencyHours;
    private String riskLevel;
    private Map<String, Object> eventData;
}
