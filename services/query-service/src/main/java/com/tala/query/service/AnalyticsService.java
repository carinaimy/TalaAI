package com.tala.query.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Analytics Service using ClickHouse
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {
    
    private final JdbcTemplate jdbcTemplate;
    
    /**
     * Get daily summary for a profile
     */
    public List<Map<String, Object>> getDailySummary(Long profileId, LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT 
                event_date,
                event_type,
                event_count,
                avg_duration,
                avg_amount
            FROM tala_analytics.daily_summary_mv
            WHERE profile_id = ?
              AND event_date BETWEEN ? AND ?
            ORDER BY event_date DESC, event_type
            """;
        
        return jdbcTemplate.queryForList(sql, profileId, startDate, endDate);
    }
    
    /**
     * Get weekly patterns
     */
    public List<Map<String, Object>> getWeeklyPatterns(Long profileId, String eventType) {
        String sql = """
            SELECT 
                day_of_week,
                hour_of_day,
                countMerge(event_count) as total_events,
                avgMerge(avg_duration) as avg_duration
            FROM tala_analytics.weekly_patterns_mv
            WHERE profile_id = ?
              AND event_type = ?
            GROUP BY day_of_week, hour_of_day
            ORDER BY day_of_week, hour_of_day
            """;
        
        return jdbcTemplate.queryForList(sql, profileId, eventType);
    }
    
    /**
     * Get event statistics
     */
    public Map<String, Object> getEventStats(Long profileId, LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT 
                count() as total_events,
                uniq(event_type) as event_types,
                avg(duration_minutes) as avg_duration,
                max(event_time) as latest_event
            FROM tala_analytics.events_analytics
            WHERE profile_id = ?
              AND event_date BETWEEN ? AND ?
            """;
        
        return jdbcTemplate.queryForMap(sql, profileId, startDate, endDate);
    }
}
