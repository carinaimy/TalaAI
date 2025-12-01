package com.tala.origindata.controller;

import com.tala.origindata.dto.TimelineEntryResponse;
import com.tala.origindata.service.TimelineService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 * Timeline Controller
 * 
 * Provides API endpoints for accessing timeline entries
 */
@RestController
@RequestMapping("/api/v1/timeline")
public class TimelineController {
    
    private final TimelineService timelineService;
    
    public TimelineController(TimelineService timelineService) {
        this.timelineService = timelineService;
    }
    
    @GetMapping("/profile/{profileId}")
    public ResponseEntity<Page<TimelineEntryResponse>> getTimelineByProfile(
            @PathVariable Long profileId,
            Pageable pageable) {
        Page<TimelineEntryResponse> timeline = timelineService.toResponsePage(
            timelineService.getTimelineByProfile(profileId, pageable)
        );
        return ResponseEntity.ok(timeline);
    }
    
    @GetMapping("/profile/{profileId}/range")
    public ResponseEntity<List<TimelineEntryResponse>> getTimelineByProfileAndTimeRange(
            @PathVariable Long profileId,
            @RequestParam Instant startTime,
            @RequestParam Instant endTime) {
        List<TimelineEntryResponse> timeline = timelineService.toResponseList(
            timelineService.getTimelineByProfileAndTimeRange(profileId, startTime, endTime)
        );
        return ResponseEntity.ok(timeline);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TimelineEntryResponse> getTimelineEntry(@PathVariable Long id) {
        return timelineService.getTimelineEntryById(id)
                .map(timelineService::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/profile/{profileId}/count")
    public ResponseEntity<Long> countByProfile(@PathVariable Long profileId) {
        long count = timelineService.countByProfile(profileId);
        return ResponseEntity.ok(count);
    }
}
