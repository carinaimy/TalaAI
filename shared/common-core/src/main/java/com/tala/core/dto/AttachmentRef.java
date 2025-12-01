package com.tala.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Unified attachment reference DTO
 * 
 * Used across all services to represent file/media attachments.
 * Points to file-service or media-service resources.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentRef {
    
    /**
     * Source system: FILE_SERVICE, MEDIA_SERVICE, EXTERNAL_URL
     */
    private String source;
    
    /**
     * Resource ID in the source system (fileId or mediaId)
     */
    private Long resourceId;
    
    /**
     * Direct access URL (generated from file-service or media-service)
     */
    private String url;
    
    /**
     * Thumbnail URL (optional, for images/videos)
     */
    private String thumbnailUrl;
    
    /**
     * MIME type (e.g., image/jpeg, video/mp4, application/pdf)
     */
    private String mediaType;
    
    /**
     * Human-readable label (optional)
     */
    private String label;
    
    /**
     * File size in bytes (optional)
     */
    private Long sizeBytes;
}
