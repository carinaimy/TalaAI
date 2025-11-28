package com.tala.event.controller;

import com.tala.event.dto.CreateEventRequest;
import com.tala.event.dto.EventResponse;
import com.tala.event.dto.UpdateEventRequest;
import com.tala.event.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 * REST API for events
 */
@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {
    
    private final EventService service;
    
    /**
     * Create new event
     */
    @PostMapping
    public ResponseEntity<EventResponse> create(
        @Valid @RequestBody CreateEventRequest request
    ) {
        log.info("POST /api/v1/events - Creating event: {}", request.getEventType());
        EventResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get event by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getById(@PathVariable Long id) {
        log.debug("GET /api/v1/events/{} - Fetching event", id);
        EventResponse response = service.getById(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update event
     */
    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> update(
        @PathVariable Long id,
        @Valid @RequestBody UpdateEventRequest request
    ) {
        log.info("PUT /api/v1/events/{} - Updating event", id);
        EventResponse response = service.update(id, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete event (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/v1/events/{} - Deleting event", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get events by profile and time range
     */
    @GetMapping
    public ResponseEntity<List<EventResponse>> getByTimeRange(
        @RequestParam Long profileId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime,
        @RequestParam(required = false) String eventType
    ) {
        log.debug("GET /api/v1/events - profileId={}, start={}, end={}, type={}", 
            profileId, startTime, endTime, eventType);
        
        List<EventResponse> events;
        if (eventType != null) {
            events = service.getByProfileTypeAndTimeRange(
                profileId, eventType, startTime, endTime
            );
        } else {
            events = service.getByProfileAndTimeRange(
                profileId, startTime, endTime
            );
        }
        
        return ResponseEntity.ok(events);
    }
    
    /**
     * Get recent events with pagination
     */
    @GetMapping("/recent")
    public ResponseEntity<Page<EventResponse>> getRecentEvents(
        @RequestParam Long profileId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        log.debug("GET /api/v1/events/recent - profileId={}, page={}, size={}", 
            profileId, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<EventResponse> events = service.getRecentEvents(profileId, pageable);
        return ResponseEntity.ok(events);
    }
    
    /**
     * Count events
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countEvents(
        @RequestParam Long profileId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime
    ) {
        log.debug("GET /api/v1/events/count - profileId={}, start={}, end={}", 
            profileId, startTime, endTime);
        
        long count = service.countByProfileAndTimeRange(profileId, startTime, endTime);
        return ResponseEntity.ok(count);
    }
}
