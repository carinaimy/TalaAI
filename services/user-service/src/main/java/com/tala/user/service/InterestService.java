package com.tala.user.service;

import com.tala.user.domain.UserInterestProfile;
import com.tala.user.dto.InterestScoresResponse;
import com.tala.user.repository.UserInterestProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

/**
 * User interest tracking service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InterestService {
    
    private final UserInterestProfileRepository repository;
    
    private static final double DECAY_FACTOR = 0.95;
    private static final int MAX_RECENT_TOPICS = 10;
    
    /**
     * Get interest scores for user and profile
     */
    @Transactional(readOnly = true)
    public InterestScoresResponse getInterestScores(Long userId, Long profileId) {
        log.debug("Getting interest scores for userId={}, profileId={}", userId, profileId);
        
        Optional<UserInterestProfile> profileOpt = repository.findByUserIdAndProfileId(userId, profileId);
        
        if (profileOpt.isEmpty()) {
            // Return default interest profile
            return InterestScoresResponse.builder()
                .userId(userId)
                .profileId(profileId)
                .interestVector(getDefaultInterestVector())
                .explicitTopics(new ArrayList<>())
                .recentTopics(new ArrayList<>())
                .build();
        }
        
        UserInterestProfile profile = profileOpt.get();
        
        return InterestScoresResponse.builder()
            .userId(userId)
            .profileId(profileId)
            .interestVector(profile.getInterestVector() != null ? 
                profile.getInterestVector() : getDefaultInterestVector())
            .explicitTopics(profile.getExplicitTopics() != null ? 
                profile.getExplicitTopics() : new ArrayList<>())
            .recentTopics(profile.getRecentTopics() != null ? 
                profile.getRecentTopics() : new ArrayList<>())
            .build();
    }
    
    /**
     * Track user interaction with a topic
     */
    @Transactional
    public void trackInteraction(Long userId, Long profileId, String topic, Double weight) {
        log.debug("Tracking interaction: userId={}, profileId={}, topic={}, weight={}", 
            userId, profileId, topic, weight);
        
        UserInterestProfile profile = repository.findByUserIdAndProfileId(userId, profileId)
            .orElseGet(() -> createNewProfile(userId, profileId));
        
        // Update interest vector
        Map<String, Double> interestVector = profile.getInterestVector();
        if (interestVector == null) {
            interestVector = getDefaultInterestVector();
        }
        
        // Apply decay to all scores
        for (Map.Entry<String, Double> entry : interestVector.entrySet()) {
            entry.setValue(entry.getValue() * DECAY_FACTOR);
        }
        
        // Boost the interacted topic
        double currentScore = interestVector.getOrDefault(topic, 0.5);
        double newScore = Math.min(currentScore + (weight * 0.1), 1.0);
        interestVector.put(topic, newScore);
        
        profile.setInterestVector(interestVector);
        
        // Update recent topics
        List<String> recentTopics = profile.getRecentTopics();
        if (recentTopics == null) {
            recentTopics = new ArrayList<>();
        }
        
        // Remove if already exists and add to front
        recentTopics.remove(topic);
        recentTopics.add(0, topic);
        
        // Keep only MAX_RECENT_TOPICS
        if (recentTopics.size() > MAX_RECENT_TOPICS) {
            recentTopics = recentTopics.subList(0, MAX_RECENT_TOPICS);
        }
        
        profile.setRecentTopics(recentTopics);
        
        repository.save(profile);
    }
    
    /**
     * Set explicit tracking topics
     */
    @Transactional
    public void setExplicitTopics(Long userId, Long profileId, List<String> topics) {
        log.debug("Setting explicit topics: userId={}, profileId={}, topics={}", 
            userId, profileId, topics);
        
        UserInterestProfile profile = repository.findByUserIdAndProfileId(userId, profileId)
            .orElseGet(() -> createNewProfile(userId, profileId));
        
        profile.setExplicitTopics(topics);
        
        // Boost interest scores for explicit topics
        Map<String, Double> interestVector = profile.getInterestVector();
        if (interestVector == null) {
            interestVector = getDefaultInterestVector();
        }
        
        for (String topic : topics) {
            interestVector.put(topic, Math.min(interestVector.getOrDefault(topic, 0.5) + 0.3, 1.0));
        }
        
        profile.setInterestVector(interestVector);
        
        repository.save(profile);
    }
    
    private UserInterestProfile createNewProfile(Long userId, Long profileId) {
        return UserInterestProfile.builder()
            .userId(userId)
            .profileId(profileId)
            .interestVector(getDefaultInterestVector())
            .explicitTopics(new ArrayList<>())
            .recentTopics(new ArrayList<>())
            .build();
    }
    
    private Map<String, Double> getDefaultInterestVector() {
        Map<String, Double> vector = new HashMap<>();
        // Default neutral scores for all topics
        vector.put("sleep", 0.7);
        vector.put("food", 0.7);
        vector.put("health", 0.8);
        vector.put("development", 0.6);
        vector.put("social", 0.5);
        vector.put("activity", 0.5);
        vector.put("mood", 0.6);
        return vector;
    }
}
