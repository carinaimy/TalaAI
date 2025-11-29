package com.tala.personalization.controller;

import com.tala.personalization.dto.InsightsPageResponse;
import com.tala.personalization.dto.TalaStartersResponse;
import com.tala.personalization.dto.TodayPageResponse;
import com.tala.personalization.service.InsightsOrchestrator;
import com.tala.personalization.service.PersonalizationOrchestrator;
import com.tala.personalization.service.TalaStartersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Personalization REST API Controller
 */
@RestController
@RequestMapping("/api/v1/personalization")
@RequiredArgsConstructor
@Slf4j
public class PersonalizationController {
    
    private final PersonalizationOrchestrator orchestrator;
    private final InsightsOrchestrator insightsOrchestrator;
    private final TalaStartersService talaStartersService;
    
    /**
     * Get Today Menu page
     */
    @GetMapping("/today")
    @Cacheable(value = "today-page", key = "#userId + '-' + #profileId + '-' + #date")
    public ResponseEntity<TodayPageResponse> getTodayPage(
        @RequestParam Long userId,
        @RequestParam Long profileId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        log.info("GET /api/v1/personalization/today - userId={}, profileId={}, date={}", 
            userId, profileId, date);
        
        if (date == null) {
            date = LocalDate.now();
        }
        
        TodayPageResponse response = orchestrator.buildTodayPage(userId, profileId, date);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get Insights Menu page
     */
    @GetMapping("/insights")
    @Cacheable(value = "insights", key = "#userId + '-' + #profileId + '-' + #date")
    public ResponseEntity<InsightsPageResponse> getInsightsPage(
        @RequestParam Long userId,
        @RequestParam Long profileId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        log.info("GET /api/v1/personalization/insights - userId={}, profileId={}, date={}", 
            userId, profileId, date);
        
        if (date == null) {
            date = LocalDate.now();
        }
        
        InsightsPageResponse response = insightsOrchestrator.buildInsightsPage(userId, profileId, date);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get Tala conversation starters
     */
    @GetMapping("/tala-starters")
    @Cacheable(value = "tala-starters", key = "#userId + '-' + #profileId + '-' + #date")
    public ResponseEntity<TalaStartersResponse> getTalaStarters(
        @RequestParam Long userId,
        @RequestParam Long profileId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        log.info("GET /api/v1/personalization/tala-starters - userId={}, profileId={}, date={}", 
            userId, profileId, date);
        
        if (date == null) {
            date = LocalDate.now();
        }
        
        TalaStartersResponse response = talaStartersService.buildTalaStarters(userId, profileId, date);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Personalization Service is running");
    }
}
