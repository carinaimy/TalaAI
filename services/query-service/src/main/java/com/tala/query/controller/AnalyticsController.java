package com.tala.query.controller;

import com.tala.query.dto.DailyContextResponse;
import com.tala.query.service.AnalyticsService;
import com.tala.query.service.DailyAggregationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Analytics and daily aggregation API
 */
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {
    
    private final AnalyticsService analyticsService;
    private final DailyAggregationService aggregationService;
    
    @GetMapping("/health")
    public String health() {
        return "Query Service is running";
    }
    
    /**
     * Get daily context for AI services
     */
    @GetMapping("/daily-context")
    public ResponseEntity<DailyContextResponse> getDailyContext(
        @RequestParam Long profileId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        log.debug("GET /api/v1/analytics/daily-context - profileId={}, date={}", profileId, date);
        DailyContextResponse context = aggregationService.getDailyContext(profileId, date);
        return ResponseEntity.ok(context);
    }
    
    /**
     * Get recent summaries
     */
    @GetMapping("/recent-summaries")
    public ResponseEntity<List<DailyContextResponse>> getRecentSummaries(
        @RequestParam Long profileId,
        @RequestParam(defaultValue = "7") int days
    ) {
        log.debug("GET /api/v1/analytics/recent-summaries - profileId={}, days={}", profileId, days);
        List<DailyContextResponse> summaries = aggregationService.getRecentSummaries(profileId, days);
        return ResponseEntity.ok(summaries);
    }
    
    @GetMapping("/daily-summary")
    public ResponseEntity<List<Map<String, Object>>> getDailySummary(
        @RequestParam Long profileId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("GET /api/v1/analytics/daily-summary - profileId={}", profileId);
        List<Map<String, Object>> summary = analyticsService.getDailySummary(profileId, startDate, endDate);
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/patterns")
    public ResponseEntity<List<Map<String, Object>>> getPatterns(
        @RequestParam Long profileId,
        @RequestParam String eventType
    ) {
        log.info("GET /api/v1/analytics/patterns - profileId={}, type={}", profileId, eventType);
        List<Map<String, Object>> patterns = analyticsService.getWeeklyPatterns(profileId, eventType);
        return ResponseEntity.ok(patterns);
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(
        @RequestParam Long profileId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("GET /api/v1/analytics/stats - profileId={}", profileId);
        Map<String, Object> stats = analyticsService.getEventStats(profileId, startDate, endDate);
        return ResponseEntity.ok(stats);
    }
}
