package com.tala.personalization.service;

import com.tala.personalization.client.QueryServiceClient;
import com.tala.personalization.dto.InsightsPageResponse;
import com.tala.personalization.dto.PersonalizationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Insights page orchestration service
 * Generates personalized insights based on data patterns and trends
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InsightsOrchestrator {
    
    private final ContextBuilder contextBuilder;
    private final PriorityCalculator priorityCalculator;
    private final UrgencyCalculator urgencyCalculator;
    private final QueryServiceClient queryServiceClient;
    
    private static final List<String> INSIGHT_CATEGORIES = Arrays.asList(
        "sleep", "food", "health", "development", "social", "activity", "mood"
    );
    
    /**
     * Build Insights page
     */
    public InsightsPageResponse buildInsightsPage(Long userId, Long profileId, LocalDate date) {
        log.info("Building Insights page for user={}, profile={}, date={}", userId, profileId, date);
        
        // Build context
        PersonalizationContext context = contextBuilder.buildContext(userId, profileId, date);
        
        // Generate insights for each category
        List<InsightsPageResponse.InsightCard> insights = new ArrayList<>();
        
        for (String category : INSIGHT_CATEGORIES) {
            InsightsPageResponse.InsightCard insight = generateInsightCard(category, context);
            if (insight != null) {
                insights.add(insight);
            }
        }
        
        // Sort by priority and urgency
        insights.sort((a, b) -> {
            int priorityCompare = Integer.compare(
                b.getPriorityScore() != null ? b.getPriorityScore() : 0,
                a.getPriorityScore() != null ? a.getPriorityScore() : 0
            );
            if (priorityCompare != 0) return priorityCompare;
            
            return Integer.compare(
                b.getUrgency() != null ? b.getUrgency() : 0,
                a.getUrgency() != null ? a.getUrgency() : 0
            );
        });
        
        return InsightsPageResponse.builder()
            .profileId(profileId)
            .insights(insights)
            .build();
    }
    
    /**
     * Generate insight card for a category
     */
    private InsightsPageResponse.InsightCard generateInsightCard(
        String category, PersonalizationContext context) {
        
        try {
            // Calculate scores
            int priorityScore = priorityCalculator.calculateTopicPriority(category, context);
            int urgency = urgencyCalculator.calculateTopicUrgency(category, context);
            
            // Skip low-priority insights
            if (priorityScore < 20 && urgency < 3) {
                return null;
            }
            
            // Get trend data
            String trend = determineTrend(category, context);
            
            // Generate data points for visualization
            List<InsightsPageResponse.DataPoint> dataPoints = generateDataPoints(category, context);
            
            // Generate conversation starters
            List<String> conversationStarters = generateConversationStarters(category, context);
            
            return InsightsPageResponse.InsightCard.builder()
                .insightId(UUID.randomUUID().toString())
                .category(category)
                .title(generateInsightTitle(category, trend, context))
                .summary(generateInsightSummary(category, trend, context))
                .priority(mapPriorityLevel(priorityScore))
                .urgency(urgency)
                .trend(trend)
                .dataPoints(dataPoints)
                .chartType(determineChartType(category))
                .actionable(isActionable(category, trend, urgency))
                .suggestedAction(generateSuggestedAction(category, trend, context))
                .conversationStarters(conversationStarters)
                .calculatedDate(context.getDate())
                .priorityScore(priorityScore)
                .build();
                
        } catch (Exception e) {
            log.warn("Failed to generate insight for category {}: {}", category, e.getMessage());
            return null;
        }
    }
    
    private String determineTrend(String category, PersonalizationContext context) {
        if (context.getTopicTrends() != null && context.getTopicTrends().containsKey(category)) {
            return context.getTopicTrends().get(category);
        }
        
        // Analyze from daily context trends
        if (context.getDailyContext() != null && 
            context.getDailyContext().getRecentTrends() != null) {
            
            Optional<PersonalizationContext.TrendData> trendData = 
                context.getDailyContext().getRecentTrends().stream()
                    .filter(t -> t.getMetric().toLowerCase().contains(category.toLowerCase()))
                    .findFirst();
            
            if (trendData.isPresent()) {
                return trendData.get().getTrend();
            }
        }
        
        return "stable";
    }
    
    private List<InsightsPageResponse.DataPoint> generateDataPoints(
        String category, PersonalizationContext context) {
        
        List<InsightsPageResponse.DataPoint> dataPoints = new ArrayList<>();
        
        // Get recent summaries for the past 7 days
        try {
            List<QueryServiceClient.DailyContextResponse> summaries = 
                queryServiceClient.getRecentSummaries(context.getProfileId(), 7);
            
            for (QueryServiceClient.DailyContextResponse summary : summaries) {
                Double value = extractMetricValue(category, summary);
                if (value != null) {
                    dataPoints.add(InsightsPageResponse.DataPoint.builder()
                        .date(summary.date)
                        .value(value)
                        .label(category)
                        .metadata(new HashMap<>())
                        .build());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to generate data points for {}: {}", category, e.getMessage());
        }
        
        return dataPoints;
    }
    
    private Double extractMetricValue(String category, QueryServiceClient.DailyContextResponse summary) {
        if (summary.metrics == null) {
            return null;
        }
        
        // Try to find metric matching category
        for (Map.Entry<String, Object> entry : summary.metrics.entrySet()) {
            if (entry.getKey().toLowerCase().contains(category.toLowerCase())) {
                if (entry.getValue() instanceof Number) {
                    return ((Number) entry.getValue()).doubleValue();
                }
            }
        }
        
        return null;
    }
    
    private String determineChartType(String category) {
        return switch (category.toLowerCase()) {
            case "sleep", "food", "activity" -> "line";
            case "mood", "health" -> "bar";
            case "development" -> "line";
            default -> "line";
        };
    }
    
    private boolean isActionable(String category, String trend, int urgency) {
        // Declining trends or high urgency are actionable
        return "declining".equalsIgnoreCase(trend) || urgency >= 6;
    }
    
    private String generateInsightTitle(String category, String trend, PersonalizationContext context) {
        String babyName = context.getBabyName() != null ? context.getBabyName() : "Baby";
        
        return switch (category.toLowerCase()) {
            case "sleep" -> trend.equals("declining") ? 
                babyName + "'s Sleep Pattern Needs Attention" : 
                babyName + "'s Sleep Pattern";
            case "food" -> trend.equals("declining") ? 
                "Appetite Changes Detected" : 
                "Eating Habits Overview";
            case "health" -> "Health Status";
            case "development" -> "Development Progress";
            case "social" -> "Social Interactions";
            case "activity" -> "Activity Level";
            case "mood" -> "Mood Patterns";
            default -> category + " Insights";
        };
    }
    
    private String generateInsightSummary(String category, String trend, PersonalizationContext context) {
        String trendDesc = switch (trend.toLowerCase()) {
            case "improving" -> "showing positive improvement";
            case "declining" -> "showing concerning decline";
            case "stable" -> "remaining stable";
            default -> "being monitored";
        };
        
        return String.format("%s patterns are %s over the past week.", 
            capitalize(category), trendDesc);
    }
    
    private String generateSuggestedAction(String category, String trend, PersonalizationContext context) {
        if ("declining".equalsIgnoreCase(trend)) {
            return switch (category.toLowerCase()) {
                case "sleep" -> "Consider reviewing bedtime routine and sleep environment";
                case "food" -> "Monitor meal times and food preferences, consult pediatrician if persists";
                case "health" -> "Schedule a check-up with pediatrician";
                case "mood" -> "Increase one-on-one time and observe for triggers";
                default -> "Monitor closely and consult with healthcare provider if concerned";
            };
        } else if ("improving".equalsIgnoreCase(trend)) {
            return "Continue current approach and maintain consistency";
        }
        
        return "Keep monitoring and maintain current routine";
    }
    
    private List<String> generateConversationStarters(String category, PersonalizationContext context) {
        List<String> starters = new ArrayList<>();
        String babyName = context.getBabyName() != null ? context.getBabyName() : "my baby";
        
        switch (category.toLowerCase()) {
            case "sleep":
                starters.add("What's a good bedtime routine for " + babyName + "?");
                starters.add("How can I improve " + babyName + "'s sleep quality?");
                starters.add("Is " + babyName + " getting enough sleep?");
                break;
            case "food":
                starters.add("What are healthy meal options for " + babyName + "?");
                starters.add("How can I encourage better eating habits?");
                starters.add("Is " + babyName + "'s diet balanced?");
                break;
            case "development":
                starters.add("What milestones should " + babyName + " reach soon?");
                starters.add("How can I support " + babyName + "'s development?");
                starters.add("Is " + babyName + "'s development on track?");
                break;
            case "social":
                starters.add("How can I help " + babyName + " make friends?");
                starters.add("What social activities are good for " + babyName + "?");
                break;
            default:
                starters.add("Tell me more about " + babyName + "'s " + category);
        }
        
        return starters;
    }
    
    private String mapPriorityLevel(int score) {
        if (score >= 75) return "high";
        if (score >= 50) return "medium";
        return "low";
    }
    
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
