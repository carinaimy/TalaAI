package com.tala.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Today At a Glance response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TodayOverviewResponse {
    
    private Long profileId;
    private LocalDate date;
    private String summarySentence;
    private String actionSuggestion;
    private List<PillTopic> pillTopics;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PillTopic {
        private String title;
        private String topic;
        private String priority;
        private String description;
    }
}
