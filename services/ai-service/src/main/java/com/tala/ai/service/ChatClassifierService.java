package com.tala.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tala.ai.dto.ChatClassificationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Stage 2: Chat Classifier Service
 * 
 * Determines user intent and interaction type:
 * - DATA_RECORDING: User wants to log baby data
 * - QUESTION_ANSWERING: User is asking questions
 * - GENERAL_CHAT: General conversation
 * - OUT_OF_SCOPE: Unrelated topics
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatClassifierService {
    
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;
    
    /**
     * Classify user chat to determine interaction type
     * 
     * @param userInput User's message
     * @param attachmentContext Context from parsed attachments (if any)
     * @param chatHistory Recent chat history for context
     * @return Classification result
     */
    public ChatClassificationResult classifyChat(String userInput, 
                                                  String attachmentContext,
                                                  String chatHistory) {
        log.info("Classifying user input: {}", userInput != null ? userInput.substring(0, Math.min(50, userInput.length())) : "null");
        
        if (userInput == null || userInput.trim().isEmpty()) {
            return ChatClassificationResult.builder()
                    .interactionType(ChatClassificationResult.InteractionType.OUT_OF_SCOPE)
                    .classificationReason("Empty input")
                    .confidence(1.0)
                    .userInput(userInput)
                    .build();
        }
        
        try {
            // Build classification prompt
            String prompt = buildClassificationPrompt(userInput, attachmentContext, chatHistory);
            
            // Call Gemini
            String aiResponse = geminiService.generateContent(prompt);
            
            // Parse response
            ChatClassificationResult result = parseClassificationResponse(aiResponse, userInput);
            result.setRawAiResponse(aiResponse);
            
            return result;
            
        } catch (Exception e) {
            log.error("Failed to classify chat", e);
            // Default to QUESTION_ANSWERING as safe fallback
            return ChatClassificationResult.builder()
                    .interactionType(ChatClassificationResult.InteractionType.QUESTION_ANSWERING)
                    .classificationReason("Classification failed, defaulting to Q&A: " + e.getMessage())
                    .confidence(0.3)
                    .userInput(userInput)
                    .aiThinkProcess("Error: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Build classification prompt
     */
    private String buildClassificationPrompt(String userInput, String attachmentContext, String chatHistory) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("""
            You are a chat classifier for a baby tracking application.
            Classify user input by SEMANTIC MEANING and CONTEXT, not just keywords.
            
            INTERACTION TYPES:
            
            1) DATA_RECORDING
               - User is reporting/logging baby events (feeding, sleep, diaper, activities, milestones, health)
               - Short factual answers to clarification questions about events
               - Examples: "Baby drank 120ml", "She slept from 2pm to 4pm", "Changed diaper at 3pm"
            
            2) QUESTION_ANSWERING
               - User is asking for information, advice, or analysis
               - Questions about baby's data, patterns, or parenting advice
               - Examples: "How much did baby eat today?", "Is the sleep pattern normal?", "What should I do about teething?"
            
            3) GENERAL_CHAT
               - Casual conversation, greetings, or emotional support
               - Examples: "Hello", "Thank you", "I'm feeling overwhelmed"
            
            4) OUT_OF_SCOPE
               - Topics unrelated to baby care or parenting
               - Examples: "What's the weather?", "Tell me a joke"
            
            KEY RULES:
            - "Baby drank 200ml" (statement) → DATA_RECORDING
            - "How much did baby drink?" (question) → QUESTION_ANSWERING
            - If user has attachments (daycare report, medical record), likely DATA_RECORDING
            - Messages ending with "?" are usually QUESTION_ANSWERING unless they're meta-commands
            
            """);
        
        // Add attachment context if available
        if (attachmentContext != null && !attachmentContext.isBlank()) {
            prompt.append("\n=== ATTACHMENT CONTEXT ===\n");
            prompt.append(attachmentContext).append("\n\n");
            prompt.append("⚠️ User has attachments. If they contain structured data (daycare report, medical record), ");
            prompt.append("this is likely DATA_RECORDING intent.\n\n");
        }
        
        // Add chat history if available
        if (chatHistory != null && !chatHistory.isBlank()) {
            prompt.append("\n=== RECENT CHAT HISTORY ===\n");
            prompt.append(chatHistory).append("\n\n");
            prompt.append("⚠️ Use chat history to understand context and continuation.\n\n");
        }
        
        prompt.append("""
            
            OUTPUT (JSON ONLY):
            Return exactly ONE JSON object. No extra text.
            {
              "interaction_type": "DATA_RECORDING|QUESTION_ANSWERING|GENERAL_CHAT|OUT_OF_SCOPE",
              "reason": "short explanation based on meaning and context",
              "confidence": 0.0-1.0,
              "ai_think_process": "your reasoning"
            }
            
            USER INPUT: "
            """);
        prompt.append(userInput).append("\"");
        
        return prompt.toString();
    }
    
    /**
     * Parse AI classification response
     */
    private ChatClassificationResult parseClassificationResponse(String aiResponse, String userInput) throws Exception {
        String json = extractJson(aiResponse);
        JsonNode root = objectMapper.readTree(json);
        
        String interactionTypeStr = root.path("interaction_type").asText();
        String reason = root.path("reason").asText("AI classification");
        double confidence = root.path("confidence").asDouble(0.8);
        String aiThinkProcess = root.path("ai_think_process").asText("");
        
        ChatClassificationResult.InteractionType interactionType = 
                ChatClassificationResult.InteractionType.valueOf(interactionTypeStr);
        
        return ChatClassificationResult.builder()
                .interactionType(interactionType)
                .classificationReason(reason)
                .confidence(confidence)
                .aiThinkProcess(aiThinkProcess)
                .userInput(userInput)
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
