package com.tala.personalization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Complete Today Menu page response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodayPageResponse {
    
    private LocalDate date;
    private Long profileId;
    
    // Today At a Glance Section
    private AtAGlanceSection atAGlance;
    
    // Ask Baby About Section
    private List<AskBabyTopic> askBabyAbout;
    
    // Heads Up Section (Reminders)
    private List<HeadsUpItem> headsUp;
    
    // Today's Moment Section (Photos)
    private TodaysMomentSection todaysMoment;
    
    // Daytime Checkin Section
    private DaytimeCheckinSection daytimeCheckin;
    
    /**
     * Today At a Glance Section
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AtAGlanceSection {
        private List<PillTopic> pillTopics;
        private String summarySentence;
        private String actionSuggestion;
    }
    
    /**
     * Pill Topic (4-5 topics displayed as pills)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PillTopic {
        private String title;           // e.g., "Great Sleep", "Poor Appetite"
        private String topic;            // e.g., "sleep", "food"
        private String priority;         // low/medium/high/critical
        private Integer urgency;         // 0-10
        private String summary;          // 2-4 words summary
        private String icon;             // icon identifier
        private Integer priorityScore;   // calculated score for sorting
    }
    
    /**
     * Ask Baby About Topic
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AskBabyTopic {
        private String topic;            // food/news/friend/activity/incident
        private String question;         // What to ask the baby
        private String context;          // Why this question (from daycare/event)
        private String priority;         // high/medium/low
        private String source;           // daycare_report/recent_event/age_appropriate
    }
    
    /**
     * Heads Up Item (Reminders)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HeadsUpItem {
        private Long reminderId;
        private String type;             // teacher_note/medical/user_reminder/weather/vaccination
        private String title;
        private String description;
        private LocalDate dueDate;
        private String category;         // prepare/info/appointment/weather/vaccination
        private Boolean canSnooze;
        private Boolean canComplete;
    }
    
    /**
     * Today's Moment Section (Photo Gallery)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TodaysMomentSection {
        private Integer mediaCount;
        private List<Long> highlightMediaIds;
        private String source;           // user_uploaded/daycare_email/daycare_api
        private Boolean hasNewMedia;
    }
    
    /**
     * Daytime Checkin Section
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DaytimeCheckinSection {
        private Long questionId;
        private String questionText;
        private String answerType;       // scale/boolean/text/number/choice
        private List<String> choices;
        private String context;          // Why this question is asked
        private String topic;            // Related topic
    }
}
