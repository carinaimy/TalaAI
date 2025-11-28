package com.tala.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tala.ai.dto.TodayOverviewResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Today At a Glance content generation service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TodayContentService {
    
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;
    
    /**
     * Generate Today At a Glance content
     */
    public TodayOverviewResponse generateTodayOverview(Long profileId, LocalDate date) {
        log.info("Generating Today overview for profile={}, date={}", profileId, date);
        
        // TODO: Fetch daily context from query-service
        // TODO: Fetch reminders from reminder-service
        // TODO: Fetch media from media-service
        // TODO: Call LLM to generate content
        
        // Mock response for now
        List<TodayOverviewResponse.PillTopic> pills = new ArrayList<>();
        pills.add(TodayOverviewResponse.PillTopic.builder()
            .title("Great Sleep")
            .topic("sleep")
            .priority("medium")
            .description("Baby slept well last night")
            .build());
        
        pills.add(TodayOverviewResponse.PillTopic.builder()
            .title("Good Appetite")
            .topic("food")
            .priority("low")
            .description("Eating normally")
            .build());
        
        return TodayOverviewResponse.builder()
            .profileId(profileId)
            .date(date)
            .summarySentence("Baby is doing great today! Sleep and eating patterns are normal.")
            .actionSuggestion("Continue with the current routine and consider outdoor playtime.")
            .pillTopics(pills)
            .build();
    }
    
    /**
     * Generate Ask Baby About suggestions
     */
    public List<String> generateAskBabySuggestions(Long profileId, LocalDate date) {
        log.info("Generating Ask Baby suggestions for profile={}, date={}", profileId, date);
        
        // TODO: Fetch daycare report
        // TODO: Fetch recent events
        // TODO: Generate age-appropriate conversation topics
        
        List<String> suggestions = new ArrayList<>();
        suggestions.add("Ask about what they had for lunch at daycare");
        suggestions.add("Talk about their favorite friend today");
        suggestions.add("Discuss the fun activity they did");
        
        return suggestions;
    }
}
