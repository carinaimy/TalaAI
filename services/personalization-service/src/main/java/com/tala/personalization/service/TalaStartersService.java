package com.tala.personalization.service;

import com.tala.personalization.dto.PersonalizationContext;
import com.tala.personalization.dto.TalaStartersResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;

/**
 * Tala conversation starters generation service
 * Generates personalized conversation prompts based on context
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TalaStartersService {
    
    private final ContextBuilder contextBuilder;
    private final PriorityCalculator priorityCalculator;
    
    /**
     * Build Tala conversation starters
     */
    public TalaStartersResponse buildTalaStarters(Long userId, Long profileId, LocalDate date) {
        log.info("Building Tala starters for user={}, profile={}, date={}", userId, profileId, date);
        
        // Build context
        PersonalizationContext context = contextBuilder.buildContext(userId, profileId, date);
        
        // Generate starters from different sources
        List<TalaStartersResponse.ConversationStarter> starters = new ArrayList<>();
        
        // 1. Age milestone starters
        starters.addAll(generateAgeMilestoneStarters(context));
        
        // 2. Recent event starters
        starters.addAll(generateRecentEventStarters(context));
        
        // 3. Seasonal starters
        starters.addAll(generateSeasonalStarters(context));
        
        // 4. Health-related starters
        starters.addAll(generateHealthStarters(context));
        
        // 5. Development starters
        starters.addAll(generateDevelopmentStarters(context));
        
        // Calculate priority scores
        for (TalaStartersResponse.ConversationStarter starter : starters) {
            int score = calculateStarterPriority(starter, context);
            starter.setPriorityScore(score);
        }
        
        // Sort by priority
        starters.sort((a, b) -> Integer.compare(
            b.getPriorityScore() != null ? b.getPriorityScore() : 0,
            a.getPriorityScore() != null ? a.getPriorityScore() : 0
        ));
        
        // Limit to top 10 starters
        List<TalaStartersResponse.ConversationStarter> topStarters = 
            starters.stream().limit(10).toList();
        
        return TalaStartersResponse.builder()
            .profileId(profileId)
            .starters(topStarters)
            .build();
    }
    
    private List<TalaStartersResponse.ConversationStarter> generateAgeMilestoneStarters(
        PersonalizationContext context) {
        
        List<TalaStartersResponse.ConversationStarter> starters = new ArrayList<>();
        Integer ageMonths = context.getBabyAgeMonths();
        String babyName = context.getBabyName() != null ? context.getBabyName() : "my baby";
        
        if (ageMonths == null) return starters;
        
        // Key milestone ages
        if (ageMonths >= 5 && ageMonths <= 7) {
            starters.add(createStarter(
                "age_milestone",
                "Starting Solid Foods",
                "What foods should I introduce to " + babyName + " at 6 months?",
                "Baby is at the age for introducing solid foods",
                "high",
                "utensils"
            ));
        }
        
        if (ageMonths >= 11 && ageMonths <= 13) {
            starters.add(createStarter(
                "age_milestone",
                "First Steps",
                "How can I help " + babyName + " learn to walk?",
                "Baby is approaching walking milestone",
                "high",
                "baby"
            ));
        }
        
        if (ageMonths >= 17 && ageMonths <= 19) {
            starters.add(createStarter(
                "age_milestone",
                "Language Development",
                "What words should " + babyName + " be saying by 18 months?",
                "Language development milestone period",
                "medium",
                "message-circle"
            ));
        }
        
        if (ageMonths >= 23 && ageMonths <= 25) {
            starters.add(createStarter(
                "age_milestone",
                "Potty Training",
                "When should I start potty training " + babyName + "?",
                "Approaching potty training age",
                "medium",
                "droplet"
            ));
        }
        
        if (ageMonths >= 35 && ageMonths <= 37) {
            starters.add(createStarter(
                "age_milestone",
                "Preschool Readiness",
                "Is " + babyName + " ready for preschool?",
                "Approaching preschool age",
                "medium",
                "school"
            ));
        }
        
        return starters;
    }
    
    private List<TalaStartersResponse.ConversationStarter> generateRecentEventStarters(
        PersonalizationContext context) {
        
        List<TalaStartersResponse.ConversationStarter> starters = new ArrayList<>();
        String babyName = context.getBabyName() != null ? context.getBabyName() : "my baby";
        
        // Check for incidents
        if (context.getDailyContext() != null && 
            Boolean.TRUE.equals(context.getDailyContext().getHasIncident())) {
            starters.add(createStarter(
                "recent_event",
                "Recent Incident",
                "What should I do after " + babyName + "'s recent incident?",
                "An incident was reported today",
                "high",
                "alert-triangle"
            ));
        }
        
        // Check for sickness
        if (context.getDailyContext() != null && 
            Boolean.TRUE.equals(context.getDailyContext().getHasSickness())) {
            starters.add(createStarter(
                "recent_event",
                "Health Concern",
                "How can I help " + babyName + " feel better?",
                "Sickness reported recently",
                "high",
                "thermometer"
            ));
        }
        
        // Check for high-priority events
        if (context.getRecentEvents() != null) {
            long highPriorityCount = context.getRecentEvents().stream()
                .filter(e -> "high".equalsIgnoreCase(e.getPriority()) || 
                           "critical".equalsIgnoreCase(e.getPriority()))
                .count();
            
            if (highPriorityCount > 0) {
                starters.add(createStarter(
                    "recent_event",
                    "Recent Concerns",
                    "Should I be worried about " + babyName + "'s recent behavior?",
                    "Multiple high-priority events detected",
                    "medium",
                    "help-circle"
                ));
            }
        }
        
        return starters;
    }
    
    private List<TalaStartersResponse.ConversationStarter> generateSeasonalStarters(
        PersonalizationContext context) {
        
        List<TalaStartersResponse.ConversationStarter> starters = new ArrayList<>();
        String babyName = context.getBabyName() != null ? context.getBabyName() : "my baby";
        Month month = context.getDate().getMonth();
        
        switch (month) {
            case DECEMBER, JANUARY, FEBRUARY:
                starters.add(createStarter(
                    "seasonal",
                    "Winter Care",
                    "How do I keep " + babyName + " healthy during winter?",
                    "Winter season care tips",
                    "medium",
                    "snowflake"
                ));
                break;
            case JUNE, JULY, AUGUST:
                starters.add(createStarter(
                    "seasonal",
                    "Summer Safety",
                    "What sun protection does " + babyName + " need?",
                    "Summer safety and sun protection",
                    "medium",
                    "sun"
                ));
                break;
            case SEPTEMBER:
                starters.add(createStarter(
                    "seasonal",
                    "Back to School",
                    "How can I prepare " + babyName + " for daycare/school?",
                    "School season preparation",
                    "medium",
                    "backpack"
                ));
                break;
        }
        
        return starters;
    }
    
    private List<TalaStartersResponse.ConversationStarter> generateHealthStarters(
        PersonalizationContext context) {
        
        List<TalaStartersResponse.ConversationStarter> starters = new ArrayList<>();
        String babyName = context.getBabyName() != null ? context.getBabyName() : "my baby";
        Integer ageMonths = context.getBabyAgeMonths();
        
        if (ageMonths == null) return starters;
        
        // Vaccination reminders
        if (ageMonths % 6 == 0 || (ageMonths >= 11 && ageMonths <= 13)) {
            starters.add(createStarter(
                "health",
                "Vaccination Schedule",
                "What vaccinations does " + babyName + " need at this age?",
                "Vaccination milestone age",
                "high",
                "shield"
            ));
        }
        
        // General health
        starters.add(createStarter(
            "health",
            "Wellness Check",
            "What health milestones should " + babyName + " reach?",
            "General health and wellness",
            "low",
            "heart"
        ));
        
        return starters;
    }
    
    private List<TalaStartersResponse.ConversationStarter> generateDevelopmentStarters(
        PersonalizationContext context) {
        
        List<TalaStartersResponse.ConversationStarter> starters = new ArrayList<>();
        String babyName = context.getBabyName() != null ? context.getBabyName() : "my baby";
        Integer ageMonths = context.getBabyAgeMonths();
        
        if (ageMonths == null) return starters;
        
        // Cognitive development
        if (ageMonths >= 12) {
            starters.add(createStarter(
                "development",
                "Learning Activities",
                "What activities can help " + babyName + "'s development?",
                "Age-appropriate development activities",
                "medium",
                "brain"
            ));
        }
        
        // Social-emotional development
        if (ageMonths >= 18) {
            starters.add(createStarter(
                "development",
                "Emotional Growth",
                "How can I support " + babyName + "'s emotional development?",
                "Social-emotional development support",
                "medium",
                "smile"
            ));
        }
        
        return starters;
    }
    
    private TalaStartersResponse.ConversationStarter createStarter(
        String category, String title, String prompt, 
        String context, String priority, String icon) {
        
        return TalaStartersResponse.ConversationStarter.builder()
            .category(category)
            .title(title)
            .prompt(prompt)
            .context(context)
            .priority(priority)
            .icon(icon)
            .build();
    }
    
    private int calculateStarterPriority(
        TalaStartersResponse.ConversationStarter starter, 
        PersonalizationContext context) {
        
        int score = 0;
        
        // Base score from priority
        score += switch (starter.getPriority() != null ? starter.getPriority().toLowerCase() : "low") {
            case "high" -> 70;
            case "medium" -> 50;
            case "low" -> 30;
            default -> 30;
        };
        
        // Boost for recent events
        if ("recent_event".equals(starter.getCategory())) {
            score += 20;
        }
        
        // Boost for age milestones
        if ("age_milestone".equals(starter.getCategory())) {
            score += 15;
        }
        
        // Boost for health-related
        if ("health".equals(starter.getCategory())) {
            score += 10;
        }
        
        return Math.min(score, 100);
    }
}
