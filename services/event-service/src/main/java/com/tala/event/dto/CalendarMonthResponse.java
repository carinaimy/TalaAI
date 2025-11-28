package com.tala.event.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.YearMonth;
import java.util.List;

/**
 * Calendar month response with daily summaries
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CalendarMonthResponse {
    
    private YearMonth yearMonth;
    private Long profileId;
    private List<CalendarDaySummary> days;
    private Long totalEvents;
}
