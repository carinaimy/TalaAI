package com.tala.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Result from Attachment Parser Service (Stage 1)
 * Analyzes attachments and extracts content
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentParserResult {
    
    /**
     * Overall summary of all attachments combined
     */
    private String overallSummary;
    
    /**
     * Detected attachment type across all files
     */
    private AttachmentType attachmentType;
    
    /**
     * Individual attachment summaries
     */
    private List<AttachmentSummary> attachments;
    
    /**
     * AI thinking process
     */
    private String aiThinkProcess;
    
    /**
     * Raw AI response for auditing
     */
    private String rawAiResponse;
    
    /**
     * Confidence score (0.0 - 1.0)
     */
    private Double confidence;
    
    /**
     * Individual attachment summary
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttachmentSummary {
        private Long attachmentId;
        private String fileName;
        private String fileType;
        private String contentSummary;
        private String extractedText;
        private List<String> keyFindings;
        private Double confidence;
        private AttachmentType detectedType;
    }
    
    /**
     * Attachment type classification
     */
    public enum AttachmentType {
        DAYCARE_REPORT,
        MEDICAL_RECORD,
        PHOTO,
        DOCUMENT,
        OTHER
    }
}
