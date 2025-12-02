package com.tala.ai.service;

import com.tala.ai.domain.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Context Enrichment Service
 * 
 * Responsible for enriching user messages with:
 * - Recent chat history
 * - Long-term memory context (from Mem0)
 * - Proper formatting for AI consumption
 * 
 * Based on reference implementation from chat-service
 * 
 * @author Tala Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ContextEnrichmentService {
    
    private final ChatMessageService chatMessageService;
    private final Mem0Service mem0Service;
    
    /**
     * Enrich user message with chat history and memory context
     * 
     * @param profileId Baby profile ID
     * @param userId User ID
     * @param userMessage Current user message
     * @param historyLimit Number of recent messages to include (default: 10)
     * @return Enriched message with [CHAT_HISTORY], [LONG_TERM_MEMORY], and [CURRENT_MESSAGE] blocks
     */
    public String enrichUserMessage(Long profileId, Long userId, String userMessage, int historyLimit) {
        log.info("Enriching user message with context: profileId={}, userId={}, historyLimit={}", 
                profileId, userId, historyLimit);
        
        // 1. Get recent chat history from database
        List<ChatMessage> recentMessages = chatMessageService.getRecentMessages(profileId, historyLimit);
        
        // 2. Build chat history block
        String messageWithHistory = buildChatHistoryBlock(userMessage, recentMessages);
        
        // 3. Get long-term memory from Mem0
        try {
            String memoryContext = mem0Service.getRelevantMemories(userMessage, userId, profileId);
            if (memoryContext != null && !memoryContext.isBlank()) {
                StringBuilder sb = new StringBuilder();
                sb.append("[LONG_TERM_MEMORY]\n");
                sb.append(memoryContext);
                sb.append("\n[/LONG_TERM_MEMORY]\n\n");
                sb.append(messageWithHistory);
                messageWithHistory = sb.toString();
                log.info("Added long-term memory context to user message");
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve long-term memory, continuing without it: {}", e.getMessage());
        }
        
        return messageWithHistory;
    }
    
    /**
     * Enrich user message with chat history only (no memory)
     * Useful when memory service is unavailable
     */
    public String enrichUserMessageWithHistory(Long profileId, String userMessage, int historyLimit) {
        log.info("Enriching user message with chat history only: profileId={}, historyLimit={}", 
                profileId, historyLimit);
        
        List<ChatMessage> recentMessages = chatMessageService.getRecentMessages(profileId, historyLimit);
        return buildChatHistoryBlock(userMessage, recentMessages);
    }
    
    /**
     * Build chat history block in format:
     * [CHAT_HISTORY]
     * User: message 1
     * Assistant: response 1
     * User: message 2
     * Assistant: response 2
     * [/CHAT_HISTORY]
     * 
     * [CURRENT_MESSAGE]
     * current user message
     * [/CURRENT_MESSAGE]
     * 
     * @param userMessage Current user message
     * @param chatHistory Recent chat messages (ordered oldest to newest)
     * @return Formatted message with history
     */
    private String buildChatHistoryBlock(String userMessage, List<ChatMessage> chatHistory) {
        if (chatHistory == null || chatHistory.isEmpty()) {
            return userMessage;
        }
        
        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("[CHAT_HISTORY]\n");
        
        // Messages from DB are already sorted by createdAt DESC (newest first)
        // We need to reverse them to show oldest first for AI context
        for (int i = chatHistory.size() - 1; i >= 0; i--) {
            ChatMessage msg = chatHistory.get(i);
            String role = msg.getRole() == ChatMessage.MessageRole.USER ? "User" : "Assistant";
            contextBuilder.append(role).append(": ").append(msg.getContent());
            
            // Include attachment info if present
            if (msg.hasAttachments()) {
                contextBuilder.append(" [").append(msg.getAttachmentCount()).append(" attachment(s)]");
            }
            
            contextBuilder.append("\n");
        }
        
        contextBuilder.append("[/CHAT_HISTORY]\n\n");
        contextBuilder.append("[CURRENT_MESSAGE]\n");
        contextBuilder.append(userMessage);
        contextBuilder.append("\n[/CURRENT_MESSAGE]");
        
        log.info("Built chat history block with {} messages", chatHistory.size());
        return contextBuilder.toString();
    }
    
    /**
     * Format chat history as simple string (for backward compatibility)
     * Used when we just need a text representation without structured blocks
     */
    public String formatChatHistoryAsText(List<ChatMessage> chatHistory) {
        if (chatHistory == null || chatHistory.isEmpty()) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = chatHistory.size() - 1; i >= 0; i--) {
            ChatMessage msg = chatHistory.get(i);
            String role = msg.getRole() == ChatMessage.MessageRole.USER ? "User" : "Assistant";
            sb.append(role).append(": ").append(msg.getContent()).append("\n");
        }
        
        return sb.toString();
    }
}
