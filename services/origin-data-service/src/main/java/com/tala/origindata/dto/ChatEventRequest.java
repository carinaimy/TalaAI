package com.tala.origindata.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Request DTO for chat-based event creation
 * Receives AI-processed data from ai-service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatEventRequest {
    
    private Long profileId;
    private String userMessage;
    private String aiMessage;
    private List<ExtractedEvent> events;
    private List<String> attachmentUrls;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtractedEvent {
        private String eventCategory;  // JOURNAL or HEALTH
        private String eventType;      // FEEDING, SLEEP, DIAPER, etc.
        private LocalDateTime timestamp;
        private String summary;
        private Map<String, Object> eventData;
        private Double confidence;
    }
}
