package com.tala.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Pattern Detection Service
 * 
 * Detects patterns in baby care data using statistical analysis
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PatternDetectionService {
    
    private final JdbcTemplate jdbcTemplate;
    
    /**
     * Detect sleep patterns for a profile
     */
    public List<Map<String, Object>> detectSleepPatterns(Long profileId) {
        log.info("Detecting sleep patterns for profile: {}", profileId);
        
        String sql = """
            SELECT 
                hour_of_day,
                day_of_week,
                countMerge(event_count) as sleep_count,
                avgMerge(avg_duration) as avg_duration_minutes
            FROM tala_analytics.weekly_patterns_mv
            WHERE profile_id = ?
              AND event_type = 'SLEEP'
            GROUP BY hour_of_day, day_of_week
            HAVING sleep_count > 3
            ORDER BY sleep_count DESC
            LIMIT 10
            """;
        
        return jdbcTemplate.queryForList(sql, profileId);
    }
    
    /**
     * Detect feeding patterns
     */
    public List<Map<String, Object>> detectFeedingPatterns(Long profileId) {
        log.info("Detecting feeding patterns for profile: {}", profileId);
        
        String sql = """
            SELECT 
                hour_of_day,
                countMerge(event_count) as feeding_count,
                avgMerge(avg_amount) as avg_amount_ml
            FROM tala_analytics.weekly_patterns_mv
            WHERE profile_id = ?
              AND event_type = 'FEEDING'
            GROUP BY hour_of_day
            HAVING feeding_count > 5
            ORDER BY hour_of_day
            """;
        
        return jdbcTemplate.queryForList(sql, profileId);
    }
    
    /**
     * Generate insights for a profile
     */
    public List<String> generateInsights(Long profileId, LocalDate startDate, LocalDate endDate) {
        log.info("Generating insights for profile: {}", profileId);
        
        List<String> insights = new ArrayList<>();
        
        // Get sleep statistics
        String sleepSql = """
            SELECT 
                avg(duration_minutes) as avg_sleep_duration,
                count() as total_sleep_events
            FROM tala_analytics.events_analytics
            WHERE profile_id = ?
              AND event_type IN ('SLEEP', 'NAP')
              AND event_date BETWEEN ? AND ?
            """;
        
        Map<String, Object> sleepStats = jdbcTemplate.queryForMap(sleepSql, profileId, startDate, endDate);
        Double avgSleepDuration = (Double) sleepStats.get("avg_sleep_duration");
        
        if (avgSleepDuration != null && avgSleepDuration > 0) {
            insights.add(String.format("Average sleep duration: %.1f hours", avgSleepDuration / 60));
            
            if (avgSleepDuration > 600) {
                insights.add("Great sleep patterns! Baby is getting good rest.");
            } else if (avgSleepDuration < 300) {
                insights.add("Short sleep durations detected. Consider establishing a bedtime routine.");
            }
        }
        
        // Get feeding statistics
        String feedingSql = """
            SELECT 
                count() as total_feedings,
                avg(amount) as avg_feeding_amount
            FROM tala_analytics.events_analytics
            WHERE profile_id = ?
              AND event_type = 'FEEDING'
              AND event_date BETWEEN ? AND ?
            """;
        
        Map<String, Object> feedingStats = jdbcTemplate.queryForMap(feedingSql, profileId, startDate, endDate);
        Long totalFeedings = ((Number) feedingStats.get("total_feedings")).longValue();
        
        if (totalFeedings > 0) {
            long daysInRange = endDate.toEpochDay() - startDate.toEpochDay() + 1;
            double avgFeedingsPerDay = (double) totalFeedings / daysInRange;
            insights.add(String.format("Average feedings per day: %.1f", avgFeedingsPerDay));
        }
        
        return insights;
    }
}
