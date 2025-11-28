package com.tala.personalization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Aggregated context for personalization decisions
 * This is an in-memory object that aggregates data from multiple services
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalizationContext {
    
    // User and profile info
    private Long userId;
    private Long profileId;
    private LocalDate date;
    private Integer babyAgeMonths;
    private String babyName;
    private String careEnvironment;      // home/daycare/preschool
    
    // Aggregated data from services
    private DailyContextData dailyContext;
    private List<ReminderData> activeReminders;
    private List<MediaData> todayMedia;
    private InterestProfileData interestProfile;
    private WeatherData weather;
    private DaycareReportData daycareReport;
    private List<RecentEventData> recentEvents;
    
    // Calculated scores (populated by scoring engines)
    private Map<String, Integer> topicPriorityScores;
    private Map<String, Integer> topicUrgencyScores;
    private Map<String, String> topicTrends;
    
    /**
     * Daily context from query-service
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyContextData {
        private Integer totalEvents;
        private Boolean hasIncident;
        private Boolean hasSickness;
        private Map<String, Object> eventsSummary;
        private Map<String, Object> metrics;
        private List<TrendData> recentTrends;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendData {
        private String metric;
        private String trend;            // improving/stable/declining
        private Double changePercent;
        private String description;
    }
    
    /**
     * Reminder data from reminder-service
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReminderData {
        private Long id;
        private String title;
        private String description;
        private String category;
        private LocalDate dueDate;
        private String priority;
        private Boolean canSnooze;
    }
    
    /**
     * Media data from media-service
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MediaData {
        private Long id;
        private String source;
        private String mediaType;
        private List<String> aiTags;
        private Integer emotionScore;
    }
    
    /**
     * Interest profile from user-service
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InterestProfileData {
        private Map<String, Double> interestVector;
        private List<String> explicitTopics;
        private List<String> recentTopics;
    }
    
    /**
     * Weather data from external-service
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeatherData {
        private String location;
        private Integer temperature;
        private String condition;
        private Integer uvIndex;
        private String airQuality;
        private String recommendation;
    }
    
    /**
     * Daycare report data from daycare-service
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DaycareReportData {
        private LocalDate reportDate;
        private Map<String, Object> meals;
        private Map<String, Object> naps;
        private List<String> activities;
        private String teacherNotes;
        private Boolean hasIncident;
        private String incidentDescription;
    }
    
    /**
     * Recent event data from event-service
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentEventData {
        private Long id;
        private String eventType;
        private String priority;
        private Integer urgencyHours;
        private String riskLevel;
        private LocalDate occurredAt;
    }
}
