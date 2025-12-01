package com.tala.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tala.ai.dto.EventExtractionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stage 3: Event Extraction Service
 * 
 * Extracts structured event data from user input based on:
 * - User message (text or voice transcription)
 * - Attachment content (if parsed)
 * - Chat history context
 * - Baby profile data
 * 
 * Outputs structured JSON/TOON format for origin-data-service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventExtractionService {
    
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;
    
    /**
     * Extract structured event data from user input
     * 
     * @param userMessage User's message
     * @param attachmentContext Parsed attachment content (if any)
     * @param babyProfileContext Baby profile information
     * @param chatHistory Recent chat history
     * @param userLocalTime User's local time for timestamp calculation
     * @return Extracted events in structured format
     */
    public EventExtractionResult extractEvents(String userMessage,
                                                String attachmentContext,
                                                String babyProfileContext,
                                                String chatHistory,
                                                String userLocalTime) {
        log.info("Extracting events from user input");
        
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return EventExtractionResult.builder()
                    .events(new ArrayList<>())
                    .aiMessage("I didn't receive any message. Could you please tell me what you'd like to record?")
                    .intentUnderstanding("Empty input")
                    .confidence(0.0)
                    .build();
        }
        
        try {
            // Build extraction prompt
            String prompt = buildExtractionPrompt(userMessage, attachmentContext, 
                    babyProfileContext, chatHistory, userLocalTime);
            
            // Call Gemini
            String aiResponse = geminiService.generateContent(prompt);
            
            // Parse response
            EventExtractionResult result = parseExtractionResponse(aiResponse);
            result.setRawAiResponse(aiResponse);
            
            return result;
            
        } catch (Exception e) {
            log.error("Failed to extract events", e);
            return EventExtractionResult.builder()
                    .events(new ArrayList<>())
                    .aiMessage("I'm having trouble understanding. Could you rephrase that?")
                    .intentUnderstanding("Extraction failed")
                    .confidence(0.0)
                    .aiThinkProcess("Error: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Build event extraction prompt
     */
    private String buildExtractionPrompt(String userMessage, String attachmentContext,
                                          String babyProfileContext, String chatHistory,
                                          String userLocalTime) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("""
            You are Tala, a warm and caring AI parenting companion.
            Your task is to extract structured baby event data from parent's input.
            
            YOUR IDENTITY & STYLE:
            - Warm, gentle, encouraging, and supportive
            - Use "we" instead of "you"
            - Keep replies natural and conversational (2-3 sentences)
            
            YOUR TASK:
            1. Generate a warm, empathetic response message for the parent
            2. Extract structured event data in JSON format
            
            EVENT CATEGORIES:
            - JOURNAL: Daily activities (FEEDING, SLEEP, DIAPER, PUMPING, MILESTONE, GROWTH_MEASUREMENT)
            - HEALTH: Health-related (SICKNESS, MEDICINE, MEDICAL_VISIT, VACCINATION)
            
            TIMESTAMP RULES (CRITICAL):
            - Use CURRENT SYSTEM TIME as reference for relative times
            - "just now" or no time → use current system time
            - "30 mins ago" → current time minus 30 minutes
            - "this morning at 8am" → today's date with 08:00:00
            - If attachment has specific date (e.g., "Visit Date: 2025-11-12"), use that date
            - Format: ISO8601 (YYYY-MM-DDTHH:mm:ss)
            
            """);
        
        // Add current time context
        if (userLocalTime != null && !userLocalTime.isBlank()) {
            prompt.append("⏰ CURRENT SYSTEM TIME: ").append(userLocalTime).append("\n");
            prompt.append("⚠️ ALL timestamp calculations MUST be based on this current time!\n\n");
        }
        
        // Add baby profile context
        if (babyProfileContext != null && !babyProfileContext.isBlank()) {
            prompt.append("=== BABY PROFILE ===\n");
            prompt.append(babyProfileContext).append("\n\n");
        }
        
        // Add attachment context
        if (attachmentContext != null && !attachmentContext.isBlank()) {
            prompt.append("=== ATTACHMENT CONTENT ===\n");
            prompt.append(attachmentContext).append("\n\n");
            prompt.append("⚠️ Extract events from attachment content. Use dates from attachment if specified.\n\n");
        }
        
        // Add chat history
        if (chatHistory != null && !chatHistory.isBlank()) {
            prompt.append("=== RECENT CHAT HISTORY ===\n");
            prompt.append(chatHistory).append("\n\n");
            prompt.append("⚠️ Use chat history to understand context and continuation.\n\n");
        }
        
        prompt.append("""
            
            OUTPUT FORMAT (JSON ONLY):
            Return exactly ONE JSON object. No extra text before or after.
            {
              "ai_message": "Your warm response to parent (2-3 sentences)",
              "intent_understanding": "Brief summary of what you understood",
              "confidence": 0.0-1.0,
              "events": [
                {
                  "event_category": "JOURNAL|HEALTH",
                  "event_type": "FEEDING|SLEEP|DIAPER|PUMPING|MILESTONE|GROWTH_MEASUREMENT|SICKNESS|MEDICINE|MEDICAL_VISIT|VACCINATION",
                  "timestamp": "2025-11-30T14:30:00",
                  "summary": "Brief event summary",
                  "event_data": {
                    "amount": 120,
                    "unit": "ML",
                    "feeding_type": "FORMULA",
                    "duration_minutes": 30,
                    "notes": "Additional notes"
                  },
                  "confidence": 0.95
                }
              ],
              "clarification_needed": ["Question 1?", "Question 2?"],
              "ai_think_process": "Your reasoning"
            }
            
            COMMON EVENT DATA FIELDS:
            - Feeding: amount, unit (ML/OZ), feeding_type (BREAST_MILK/FORMULA/SOLID_FOOD), food_name
            - Sleep: duration_minutes, sleep_quality (POOR/FAIR/GOOD/EXCELLENT), sleep_action (start_sleep/end_sleep/complete_sleep)
            - Diaper: diaper_type (WET/DIRTY/BOTH)
            - Milestone: milestone_type (MOTOR/LANGUAGE/SOCIAL/COGNITIVE), milestone_name
            - Medicine: medicine_name, dosage, dosage_unit
            - Medical Visit: visit_type, doctor_name, diagnosis, notes
            - Sickness: symptom_name, severity (MILD/MODERATE/SEVERE), temperature, temperature_unit (C/F)
            
            IMPORTANT:
            - If data is incomplete, add questions to clarification_needed array
            - confidence should reflect how certain you are about the extracted data
            - ai_message should always be warm and encouraging
            
            USER INPUT: "
            """);
        prompt.append(userMessage).append("\"");
        
        return prompt.toString();
    }
    
    /**
     * Parse AI extraction response
     */
    private EventExtractionResult parseExtractionResponse(String aiResponse) throws Exception {
        String json = extractJson(aiResponse);
        JsonNode root = objectMapper.readTree(json);
        
        String aiMessage = root.path("ai_message").asText("Got it! I've processed your request.");
        String intentUnderstanding = root.path("intent_understanding").asText("");
        double confidence = root.path("confidence").asDouble(0.8);
        String aiThinkProcess = root.path("ai_think_process").asText("");
        
        // Parse events
        List<EventExtractionResult.ExtractedEvent> events = new ArrayList<>();
        JsonNode eventsNode = root.path("events");
        if (eventsNode.isArray()) {
            for (JsonNode eventNode : eventsNode) {
                EventExtractionResult.ExtractedEvent event = parseEvent(eventNode);
                events.add(event);
            }
        }
        
        // Parse clarification questions
        List<String> clarifications = new ArrayList<>();
        JsonNode clarNode = root.path("clarification_needed");
        if (clarNode.isArray()) {
            for (JsonNode clar : clarNode) {
                clarifications.add(clar.asText());
            }
        }
        
        return EventExtractionResult.builder()
                .aiMessage(aiMessage)
                .intentUnderstanding(intentUnderstanding)
                .confidence(confidence)
                .events(events)
                .clarificationNeeded(clarifications)
                .aiThinkProcess(aiThinkProcess)
                .build();
    }
    
    /**
     * Parse individual event
     */
    private EventExtractionResult.ExtractedEvent parseEvent(JsonNode eventNode) {
        String eventCategory = eventNode.path("event_category").asText();
        String eventType = eventNode.path("event_type").asText();
        String timestampStr = eventNode.path("timestamp").asText();
        String summary = eventNode.path("summary").asText();
        double confidence = eventNode.path("confidence").asDouble(0.8);
        
        // Parse timestamp
        LocalDateTime timestamp = null;
        if (!timestampStr.isBlank()) {
            try {
                timestamp = LocalDateTime.parse(timestampStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (Exception e) {
                log.warn("Failed to parse timestamp: {}", timestampStr);
            }
        }
        
        // Parse event data (flexible key-value pairs)
        Map<String, Object> eventData = new HashMap<>();
        JsonNode dataNode = eventNode.path("event_data");
        if (dataNode.isObject()) {
            dataNode.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                
                if (value.isNumber()) {
                    eventData.put(key, value.asDouble());
                } else if (value.isBoolean()) {
                    eventData.put(key, value.asBoolean());
                } else {
                    eventData.put(key, value.asText());
                }
            });
        }
        
        return EventExtractionResult.ExtractedEvent.builder()
                .eventCategory(eventCategory)
                .eventType(eventType)
                .timestamp(timestamp)
                .summary(summary)
                .eventData(eventData)
                .confidence(confidence)
                .build();
    }
    
    /**
     * Extract JSON from AI response (remove markdown formatting)
     */
    private String extractJson(String response) {
        if (response == null) {
            return "{}";
        }
        
        // Try to extract from ```json ... ``` blocks
        if (response.contains("```json")) {
            int start = response.indexOf("```json") + 7;
            int end = response.lastIndexOf("```");
            if (start > 6 && end > start) {
                return response.substring(start, end).trim();
            }
        }
        
        // Try to extract from ``` ... ``` blocks
        if (response.contains("```")) {
            int start = response.indexOf("```") + 3;
            int end = response.lastIndexOf("```");
            if (start > 2 && end > start) {
                return response.substring(start, end).trim();
            }
        }
        
        // Fallback: extract JSON between first { and last }
        int firstBrace = response.indexOf('{');
        int lastBrace = response.lastIndexOf('}');
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            return response.substring(firstBrace, lastBrace + 1).trim();
        }
        
        return response.trim();
    }
}
