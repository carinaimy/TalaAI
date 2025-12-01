package com.tala.origindata.dto;

import com.tala.core.dto.AttachmentRef;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Timeline Entry Response DTO
 * 
 * Includes resolved attachments from OriginalEvent
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimelineEntryResponse {
    
    private Long id;
    private Long originalEventId;
    private Long profileId;
    private String timelineType;
    private String dataSource;
    private Instant recordTime;
    private String title;
    private String aiSummary;
    private String aiTags;
    private String location;
    private String aiModelVersion;
    private List<AttachmentRef> attachments;
    private Instant createdAt;
    private Instant updatedAt;
}
