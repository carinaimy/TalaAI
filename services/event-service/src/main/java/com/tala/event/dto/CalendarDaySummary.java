package com.tala.event.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Calendar day summary for monthly view
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CalendarDaySummary {
    
    private LocalDate date;
    private Long totalEvents;
    private Boolean hasImportantEvent;
    private Boolean hasIncident;
    private Boolean hasSickness;
    private Boolean hasReminder;
    private Boolean hasMedicalVisit;
    private Map<String, Long> eventTypeCount;
    private List<String> highlightTags;
}
