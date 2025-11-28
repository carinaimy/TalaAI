package com.tala.personalization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Tala Menu conversation starters response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TalaStartersResponse {
    
    private Long profileId;
    private List<ConversationStarter> starters;
    
    /**
     * Individual conversation starter
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConversationStarter {
        private String category;         // age_milestone/recent_event/seasonal/health/development
        private String title;
        private String prompt;           // The actual conversation starter text
        private String context;          // Why this starter is suggested
        private String priority;         // high/medium/low
        private Integer priorityScore;
        private String icon;
    }
}
