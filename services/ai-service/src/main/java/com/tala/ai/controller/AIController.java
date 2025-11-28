package com.tala.ai.controller;

import com.tala.ai.dto.TodayOverviewResponse;
import com.tala.ai.service.PatternDetectionService;
import com.tala.ai.service.TodayContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * AI REST API
 */
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Slf4j
public class AIController {
    
    private final PatternDetectionService patternDetectionService;
    private final TodayContentService todayContentService;
    
    @GetMapping("/patterns/sleep")
    public ResponseEntity<List<Map<String, Object>>> getSleepPatterns(
        @RequestParam Long profileId
    ) {
        log.info("GET /api/v1/ai/patterns/sleep - profileId={}", profileId);
        List<Map<String, Object>> patterns = patternDetectionService.detectSleepPatterns(profileId);
        return ResponseEntity.ok(patterns);
    }
    
    @GetMapping("/patterns/feeding")
    public ResponseEntity<List<Map<String, Object>>> getFeedingPatterns(
        @RequestParam Long profileId
    ) {
        log.info("GET /api/v1/ai/patterns/feeding - profileId={}", profileId);
        List<Map<String, Object>> patterns = patternDetectionService.detectFeedingPatterns(profileId);
        return ResponseEntity.ok(patterns);
    }
    
    @GetMapping("/insights")
    public ResponseEntity<List<String>> getInsights(
        @RequestParam Long profileId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("GET /api/v1/ai/insights - profileId={}", profileId);
        List<String> insights = patternDetectionService.generateInsights(profileId, startDate, endDate);
        return ResponseEntity.ok(insights);
    }
}
