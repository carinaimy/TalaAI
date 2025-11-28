package com.tala.query.controller;

import com.tala.query.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Analytics REST API
 */
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {
    
    private final AnalyticsService analyticsService;
    
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
