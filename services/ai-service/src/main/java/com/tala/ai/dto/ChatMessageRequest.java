package com.tala.ai.dto;

import com.tala.core.dto.AttachmentRef;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Chat Message Request DTO
 * 
 * Supports text + optional attachments for multimodal AI interactions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequest {
    
    /**
     * Message role: user, assistant, system
     */
    private String role;
    
    /**
     * Text content of the message
     */
    private String content;
    
    /**
     * Optional attachments (images, audio, documents)
     * References to file-service or media-service resources
     */
    private List<AttachmentRef> attachments;
    
    /**
     * Profile ID for context
     */
    private Long profileId;
}
