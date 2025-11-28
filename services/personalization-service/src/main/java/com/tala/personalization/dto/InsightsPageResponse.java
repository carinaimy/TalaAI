package com.tala.personalization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Insights Menu page response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsightsPageResponse {
    
    private Long profileId;
    private List<InsightCard> insights;
    
    /**
     * Individual Insight Card
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InsightCard {
        private String insightId;
        private String category;         // sleep/food/health/development/social
        private String title;
        private String summary;
        private String priority;         // low/medium/high
        private Integer urgency;         // 0-10
        private String trend;            // improving/stable/declining
        
        // Data visualization
        private List<DataPoint> dataPoints;
        private String chartType;        // line/bar/pie
        
        // Actionability
        private Boolean actionable;
        private String suggestedAction;
        
        // Conversation starters for Tala
        private List<String> conversationStarters;
        
        // Metadata
        private LocalDate calculatedDate;
        private Integer priorityScore;
    }
    
    /**
     * Data Point for visualization
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataPoint {
        private LocalDate date;
        private Double value;
        private String label;
        private Map<String, Object> metadata;
    }
}
