package com.tala.personalization.service;

import com.tala.personalization.dto.PersonalizationContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;

/**
 * Urgency calculation engine
 * Calculates urgency scores (0-10) for topics and events
 */
@Component
@Slf4j
public class UrgencyCalculator {
    
    /**
     * Calculate urgency for a topic (0-10)
     * 10 = immediate action required
     * 0 = no urgency
     */
    public int calculateTopicUrgency(String topic, PersonalizationContext context) {
        int urgency = 0;
        
        // 1. Check for critical events
        urgency += calculateEventUrgency(topic, context);
        
        // 2. Check for time-sensitive reminders
        urgency += calculateReminderUrgency(topic, context);
        
        // 3. Check for declining trends
        urgency += calculateTrendUrgency(topic, context);
        
        // 4. Check for daycare concerns
        urgency += calculateDaycareUrgency(topic, context);
        
        int finalUrgency = Math.min(urgency, 10);
        
        log.debug("Urgency score for topic '{}': {}", topic, finalUrgency);
        
        return finalUrgency;
    }
    
    /**
     * Calculate urgency from events
     */
    private int calculateEventUrgency(String topic, PersonalizationContext context) {
        if (context.getRecentEvents() == null || context.getRecentEvents().isEmpty()) {
            return 0;
        }
        
        int maxUrgency = 0;
        
        for (PersonalizationContext.RecentEventData event : context.getRecentEvents()) {
            // Skip if not related to topic
            if (!event.getEventType().toLowerCase().contains(topic.toLowerCase())) {
                continue;
            }
            
            int eventUrgency = calculateSingleEventUrgency(event, context.getDate());
            maxUrgency = Math.max(maxUrgency, eventUrgency);
        }
        
        return maxUrgency;
    }
    
    /**
     * Calculate urgency for a single event
     */
    private int calculateSingleEventUrgency(PersonalizationContext.RecentEventData event, LocalDate today) {
        int urgency = 0;
        
        // Base urgency from event type
        String eventType = event.getEventType().toLowerCase();
        if (eventType.contains("incident")) {
            urgency = 9;
        } else if (eventType.contains("sickness") || eventType.contains("illness")) {
            urgency = 8;
        } else if (eventType.contains("medical")) {
            urgency = 7;
        } else if (eventType.contains("injury")) {
            urgency = 8;
        } else if (eventType.contains("allergy")) {
            urgency = 7;
        }
        
        // Adjust based on priority
        if ("critical".equalsIgnoreCase(event.getPriority())) {
            urgency += 2;
        } else if ("high".equalsIgnoreCase(event.getPriority())) {
            urgency += 1;
        }
        
        // Adjust based on risk level
        if ("HIGH".equalsIgnoreCase(event.getRiskLevel())) {
            urgency += 1;
        }
        
        // Reduce urgency if event is older
        if (event.getOccurredAt() != null) {
            long daysSince = Duration.between(
                event.getOccurredAt().atStartOfDay(), 
                today.atStartOfDay()
            ).toDays();
            
            if (daysSince > 3) {
                urgency = Math.max(urgency - 2, 0);
            } else if (daysSince > 1) {
                urgency = Math.max(urgency - 1, 0);
            }
        }
        
        return Math.min(urgency, 10);
    }
    
    /**
     * Calculate urgency from reminders
     */
    private int calculateReminderUrgency(String topic, PersonalizationContext context) {
        if (context.getActiveReminders() == null || context.getActiveReminders().isEmpty()) {
            return 0;
        }
        
        int maxUrgency = 0;
        LocalDate today = context.getDate();
        
        for (PersonalizationContext.ReminderData reminder : context.getActiveReminders()) {
            if (reminder.getDueDate() == null) {
                continue;
            }
            
            long daysUntilDue = Duration.between(
                today.atStartOfDay(),
                reminder.getDueDate().atStartOfDay()
            ).toDays();
            
            int reminderUrgency = 0;
            
            // Overdue reminders
            if (daysUntilDue < 0) {
                reminderUrgency = 8;
            }
            // Due today
            else if (daysUntilDue == 0) {
                reminderUrgency = 7;
            }
            // Due tomorrow
            else if (daysUntilDue == 1) {
                reminderUrgency = 5;
            }
            // Due within 3 days
            else if (daysUntilDue <= 3) {
                reminderUrgency = 3;
            }
            // Due within a week
            else if (daysUntilDue <= 7) {
                reminderUrgency = 2;
            }
            
            // Boost for high priority reminders
            if ("high".equalsIgnoreCase(reminder.getPriority())) {
                reminderUrgency += 1;
            }
            
            // Boost for certain categories
            String category = reminder.getCategory();
            if ("appointment".equalsIgnoreCase(category) || 
                "vaccination".equalsIgnoreCase(category) ||
                "medication".equalsIgnoreCase(category)) {
                reminderUrgency += 1;
            }
            
            maxUrgency = Math.max(maxUrgency, reminderUrgency);
        }
        
        return Math.min(maxUrgency, 10);
    }
    
    /**
     * Calculate urgency from trends
     */
    private int calculateTrendUrgency(String topic, PersonalizationContext context) {
        if (context.getTopicTrends() == null) {
            return 0;
        }
        
        String trend = context.getTopicTrends().get(topic);
        if (trend == null) {
            return 0;
        }
        
        // Declining trends are urgent
        if ("declining".equalsIgnoreCase(trend)) {
            // Check if it's a critical topic
            if (topic.equalsIgnoreCase("sleep") || 
                topic.equalsIgnoreCase("food") ||
                topic.equalsIgnoreCase("health")) {
                return 6;
            }
            return 4;
        }
        
        return 0;
    }
    
    /**
     * Calculate urgency from daycare report
     */
    private int calculateDaycareUrgency(String topic, PersonalizationContext context) {
        if (context.getDaycareReport() == null) {
            return 0;
        }
        
        PersonalizationContext.DaycareReportData report = context.getDaycareReport();
        
        int urgency = 0;
        
        // Incident in daycare report
        if (Boolean.TRUE.equals(report.getHasIncident())) {
            urgency = 7;
        }
        
        // Teacher notes present (usually indicates something to address)
        if (report.getTeacherNotes() != null && !report.getTeacherNotes().isEmpty()) {
            // Check for urgent keywords
            String notes = report.getTeacherNotes().toLowerCase();
            if (notes.contains("urgent") || notes.contains("immediate") || 
                notes.contains("concern") || notes.contains("worried")) {
                urgency = Math.max(urgency, 6);
            } else {
                urgency = Math.max(urgency, 3);
            }
        }
        
        return urgency;
    }
    
    /**
     * Determine if a topic requires immediate attention
     */
    public boolean isImmediateActionRequired(String topic, PersonalizationContext context) {
        int urgency = calculateTopicUrgency(topic, context);
        return urgency >= 8;
    }
    
    /**
     * Get urgency level description
     */
    public String getUrgencyLevel(int urgency) {
        if (urgency >= 9) return "critical";
        if (urgency >= 7) return "high";
        if (urgency >= 5) return "medium";
        if (urgency >= 3) return "low";
        return "none";
    }
}
