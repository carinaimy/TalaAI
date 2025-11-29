package com.tala.user.controller;

import com.tala.user.dto.InterestScoresResponse;
import com.tala.user.service.InterestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * User Interest Profile REST API
 */
@RestController
@RequestMapping("/api/v1/users/interest")
@RequiredArgsConstructor
@Slf4j
public class InterestController {
    
    private final InterestService interestService;
    
    /**
     * Get interest scores for user and profile
     */
    @GetMapping("/scores")
    public ResponseEntity<InterestScoresResponse> getInterestScores(
        @RequestParam Long userId,
        @RequestParam Long profileId
    ) {
        log.info("GET /api/v1/users/interest/scores - userId={}, profileId={}", userId, profileId);
        InterestScoresResponse response = interestService.getInterestScores(userId, profileId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update interest scores based on user interaction
     */
    @PostMapping("/track")
    public ResponseEntity<Void> trackInteraction(
        @RequestParam Long userId,
        @RequestParam Long profileId,
        @RequestParam String topic,
        @RequestParam(defaultValue = "1.0") Double weight
    ) {
        log.info("POST /api/v1/users/interest/track - userId={}, profileId={}, topic={}", 
            userId, profileId, topic);
        interestService.trackInteraction(userId, profileId, topic, weight);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Set explicit tracking topics
     */
    @PostMapping("/explicit-topics")
    public ResponseEntity<Void> setExplicitTopics(
        @RequestParam Long userId,
        @RequestParam Long profileId,
        @RequestBody java.util.List<String> topics
    ) {
        log.info("POST /api/v1/users/interest/explicit-topics - userId={}, profileId={}", 
            userId, profileId);
        interestService.setExplicitTopics(userId, profileId, topics);
        return ResponseEntity.ok().build();
    }
}
