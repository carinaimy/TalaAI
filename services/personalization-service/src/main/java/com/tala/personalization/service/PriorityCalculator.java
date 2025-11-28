package com.tala.personalization.service;

import com.tala.personalization.dto.PersonalizationContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;

/**
 * Priority calculation engine
 * Calculates topic priority scores based on multiple factors
 */
@Component
@Slf4j
public class PriorityCalculator {
    
    @Value("${personalization.priority.user-interest-weight:0.30}")
    private double userInterestWeight;
    
    @Value("${personalization.priority.urgency-weight:0.25}")
    private double urgencyWeight;
    
    @Value("${personalization.priority.age-relevance-weight:0.20}")
    private double ageRelevanceWeight;
    
    @Value("${personalization.priority.recency-weight:0.15}")
    private double recencyWeight;
    
    @Value("${personalization.priority.trend-weight:0.10}")
    private double trendWeight;
    
    /**
     * Calculate priority score for a topic (0-100)
     */
    public int calculateTopicPriority(String topic, PersonalizationContext context) {
        double score = 0.0;
        
        // 1. User Interest Score (0-30 points)
        score += calculateUserInterestScore(topic, context) * userInterestWeight * 100;
        
        // 2. Urgency Score (0-25 points)
        score += calculateUrgencyScore(topic, context) * urgencyWeight * 100;
        
        // 3. Age Relevance Score (0-20 points)
        score += calculateAgeRelevanceScore(topic, context.getBabyAgeMonths()) * ageRelevanceWeight * 100;
        
        // 4. Recency Score (0-15 points)
        score += calculateRecencyScore(topic, context) * recencyWeight * 100;
        
        // 5. Trend Score (0-10 points)
        score += calculateTrendScore(topic, context) * trendWeight * 100;
        
        int finalScore = (int) Math.min(Math.round(score), 100);
        
        log.debug("Priority score for topic '{}': {} (interest={}, urgency={}, age={}, recency={}, trend={})",
            topic, finalScore,
            calculateUserInterestScore(topic, context),
            calculateUrgencyScore(topic, context),
            calculateAgeRelevanceScore(topic, context.getBabyAgeMonths()),
            calculateRecencyScore(topic, context),
            calculateTrendScore(topic, context));
        
        return finalScore;
    }
    
    /**
     * User interest score (0.0 - 1.0)
     */
    private double calculateUserInterestScore(String topic, PersonalizationContext context) {
        if (context.getInterestProfile() == null) {
            return 0.5; // Default neutral score
        }
        
        Map<String, Double> interestVector = context.getInterestProfile().getInterestVector();
        if (interestVector == null || !interestVector.containsKey(topic)) {
            return 0.5;
        }
        
        double baseScore = interestVector.get(topic);
        
        // Boost if in explicit tracking topics
        if (context.getInterestProfile().getExplicitTopics() != null &&
            context.getInterestProfile().getExplicitTopics().contains(topic)) {
            baseScore = Math.min(baseScore + 0.2, 1.0);
        }
        
        // Boost if in recent topics
        if (context.getInterestProfile().getRecentTopics() != null &&
            context.getInterestProfile().getRecentTopics().contains(topic)) {
            baseScore = Math.min(baseScore + 0.1, 1.0);
        }
        
        return baseScore;
    }
    
    /**
     * Urgency score based on recent events (0.0 - 1.0)
     */
    private double calculateUrgencyScore(String topic, PersonalizationContext context) {
        double score = 0.0;
        
        // Check daily context for incidents/issues
        if (context.getDailyContext() != null) {
            if (context.getDailyContext().getHasIncident()) {
                score += 0.5;
            }
            if (context.getDailyContext().getHasSickness()) {
                score += 0.5;
            }
        }
        
        // Check daycare report for concerns
        if (context.getDaycareReport() != null) {
            if (context.getDaycareReport().getHasIncident()) {
                score += 0.4;
            }
            if (context.getDaycareReport().getTeacherNotes() != null &&
                !context.getDaycareReport().getTeacherNotes().isEmpty()) {
                score += 0.2;
            }
        }
        
        // Check recent events
        if (context.getRecentEvents() != null) {
            long highPriorityEvents = context.getRecentEvents().stream()
                .filter(e -> "high".equalsIgnoreCase(e.getPriority()) || 
                           "critical".equalsIgnoreCase(e.getPriority()))
                .count();
            score += Math.min(highPriorityEvents * 0.15, 0.6);
        }
        
        return Math.min(score, 1.0);
    }
    
    /**
     * Age relevance score (0.0 - 1.0)
     * Different topics are more relevant at different ages
     */
    private double calculateAgeRelevanceScore(String topic, Integer ageMonths) {
        if (ageMonths == null) {
            return 0.5;
        }
        
        return switch (topic.toLowerCase()) {
            case "sleep" -> {
                // Sleep is always important but especially 0-12 months
                if (ageMonths < 12) yield 1.0;
                if (ageMonths < 24) yield 0.8;
                yield 0.6;
            }
            case "food", "feeding" -> {
                // Food transitions at 6, 12, 18 months
                if (ageMonths >= 5 && ageMonths <= 7) yield 1.0;
                if (ageMonths >= 11 && ageMonths <= 13) yield 1.0;
                if (ageMonths >= 17 && ageMonths <= 19) yield 1.0;
                yield 0.7;
            }
            case "development", "milestone" -> {
                // Development tracking important throughout
                if (ageMonths < 24) yield 1.0;
                if (ageMonths < 48) yield 0.9;
                yield 0.7;
            }
            case "social", "friend" -> {
                // Social becomes more important after 18 months
                if (ageMonths < 12) yield 0.3;
                if (ageMonths < 24) yield 0.6;
                yield 0.9;
            }
            case "potty", "toilet" -> {
                // Potty training relevant 18-36 months
                if (ageMonths < 18) yield 0.1;
                if (ageMonths >= 18 && ageMonths <= 36) yield 1.0;
                if (ageMonths > 36) yield 0.4;
                yield 0.2;
            }
            case "health", "medical" -> {
                // Health always important
                yield 0.9;
            }
            default -> 0.5;
        };
    }
    
    /**
     * Recency score - how recent is the last event for this topic (0.0 - 1.0)
     */
    private double calculateRecencyScore(String topic, PersonalizationContext context) {
        if (context.getRecentEvents() == null || context.getRecentEvents().isEmpty()) {
            return 0.3; // Low score if no recent events
        }
        
        LocalDate today = context.getDate();
        
        // Find most recent event for this topic
        LocalDate mostRecentDate = context.getRecentEvents().stream()
            .filter(e -> e.getEventType().toLowerCase().contains(topic.toLowerCase()))
            .map(PersonalizationContext.RecentEventData::getOccurredAt)
            .max(LocalDate::compareTo)
            .orElse(null);
        
        if (mostRecentDate == null) {
            return 0.3;
        }
        
        long daysSince = Duration.between(mostRecentDate.atStartOfDay(), today.atStartOfDay()).toDays();
        
        // Score decreases with time
        if (daysSince == 0) return 1.0;
        if (daysSince == 1) return 0.9;
        if (daysSince <= 3) return 0.7;
        if (daysSince <= 7) return 0.5;
        if (daysSince <= 14) return 0.3;
        return 0.1;
    }
    
    /**
     * Trend score - boost declining trends, moderate improving trends (0.0 - 1.0)
     */
    private double calculateTrendScore(String topic, PersonalizationContext context) {
        String trend = context.getTopicTrends() != null ? 
            context.getTopicTrends().get(topic) : null;
        
        if (trend == null) {
            return 0.5; // Neutral if no trend data
        }
        
        return switch (trend.toLowerCase()) {
            case "declining" -> 1.0;      // High priority for declining trends
            case "improving" -> 0.6;      // Moderate priority for improving (still worth noting)
            case "stable" -> 0.4;         // Lower priority for stable
            default -> 0.5;
        };
    }
}
