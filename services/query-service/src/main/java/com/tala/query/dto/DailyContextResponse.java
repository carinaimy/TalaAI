package com.tala.query.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Daily context response for AI services
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DailyContextResponse {
    
    private Long profileId;
    private LocalDate date;
    private Map<String, Object> eventsSummary;
    private Map<String, Object> metrics;
    private List<Long> candidateMediaIds;
    private List<Long> candidateIncidentIds;
    private Integer totalEvents;
    private Boolean hasIncident;
    private Boolean hasSickness;
    private List<TrendData> recentTrends;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendData {
        private String metric;
        private String trend;
        private Double changePercent;
        private String description;
    }
}
