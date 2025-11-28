package com.tala.event.service;

import com.tala.core.constant.EventTypes;
import com.tala.core.exception.ErrorCode;
import com.tala.core.exception.TalaException;
import com.tala.event.domain.Event;
import com.tala.event.dto.CreateEventRequest;
import com.tala.event.dto.EventResponse;
import com.tala.event.dto.UpdateEventRequest;
import com.tala.event.mapper.EventMapper;
import com.tala.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Event service for CRUD operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {
    
    private final EventRepository repository;
    private final EventMapper mapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    
    private static final String EVENT_TOPIC = "tala.events";
    
    /**
     * Create new event
     */
    @Transactional
    @CacheEvict(value = "events", key = "#request.profileId")
    public EventResponse create(CreateEventRequest request) {
        log.info("Creating event: type={}, profileId={}", 
            request.getEventType(), request.getProfileId());
        
        // Validate event type
        validateEventType(request.getEventType());
        
        // Validate event time
        validateEventTime(request.getEventTime());
        
        // Convert to entity
        Event event = mapper.toEntity(request);
        
        // Save to database
        Event saved = repository.save(event);
        
        // Publish to Kafka (async)
        publishEventToKafka("created", saved);
        
        log.info("Event created: id={}, type={}", saved.getId(), saved.getEventType());
        
        return mapper.toResponse(saved);
    }
    
    /**
     * Get event by ID
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "events", key = "#id")
    public EventResponse getById(Long id) {
        Event event = repository.findByIdAndNotDeleted(id)
            .orElseThrow(() -> new TalaException(
                ErrorCode.EVENT_NOT_FOUND, 
                "Event not found: " + id
            ));
        
        return mapper.toResponse(event);
    }
    
    /**
     * Update event
     */
    @Transactional
    @CacheEvict(value = "events", allEntries = true)
    public EventResponse update(Long id, UpdateEventRequest request) {
        Event event = repository.findByIdAndNotDeleted(id)
            .orElseThrow(() -> new TalaException(
                ErrorCode.EVENT_NOT_FOUND, 
                "Event not found: " + id
            ));
        
        // Validate event time if provided
        if (request.getEventTime() != null) {
            validateEventTime(request.getEventTime());
        }
        
        // Update fields
        mapper.updateEntity(request, event);
        
        // Save
        Event updated = repository.save(event);
        
        // Publish update event
        publishEventToKafka("updated", updated);
        
        return mapper.toResponse(updated);
    }
    
    /**
     * Soft delete event
     */
    @Transactional
    @CacheEvict(value = "events", allEntries = true)
    public void delete(Long id) {
        Event event = repository.findByIdAndNotDeleted(id)
            .orElseThrow(() -> new TalaException(
                ErrorCode.EVENT_NOT_FOUND, 
                "Event not found: " + id
            ));
        
        event.softDelete();
        repository.save(event);
        
        // Publish delete event
        publishEventToKafka("deleted", event);
        
        log.info("Event soft deleted: id={}", id);
    }
    
    /**
     * Get events by profile and time range
     */
    @Transactional(readOnly = true)
    public List<EventResponse> getByProfileAndTimeRange(
        Long profileId,
        Instant startTime,
        Instant endTime
    ) {
        validateTimeRange(startTime, endTime);
        
        List<Event> events = repository.findByProfileIdAndTimeRange(
            profileId, startTime, endTime
        );
        
        return mapper.toResponseList(events);
    }
    
    /**
     * Get events by profile, type and time range
     */
    @Transactional(readOnly = true)
    public List<EventResponse> getByProfileTypeAndTimeRange(
        Long profileId,
        String eventType,
        Instant startTime,
        Instant endTime
    ) {
        validateEventType(eventType);
        validateTimeRange(startTime, endTime);
        
        List<Event> events = repository.findByProfileIdAndEventTypeAndTimeRange(
            profileId, eventType, startTime, endTime
        );
        
        return mapper.toResponseList(events);
    }
    
    /**
     * Get recent events with pagination
     */
    @Transactional(readOnly = true)
    public Page<EventResponse> getRecentEvents(Long profileId, Pageable pageable) {
        Page<Event> events = repository
            .findByProfileIdAndDeletedAtIsNullOrderByEventTimeDesc(
                profileId, pageable
            );
        
        return events.map(mapper::toResponse);
    }
    
    /**
     * Count events by profile and time range
     */
    @Transactional(readOnly = true)
    public long countByProfileAndTimeRange(
        Long profileId,
        Instant startTime,
        Instant endTime
    ) {
        validateTimeRange(startTime, endTime);
        
        return repository.countByProfileIdAndTimeRange(
            profileId, startTime, endTime
        );
    }
    
    /**
     * Validate event type
     */
    private void validateEventType(String eventType) {
        if (!EventTypes.isValid(eventType)) {
            throw new TalaException(
                ErrorCode.INVALID_EVENT_TYPE,
                "Invalid event type: " + eventType
            );
        }
    }
    
    /**
     * Validate event time
     */
    private void validateEventTime(Instant eventTime) {
        if (eventTime.isAfter(Instant.now())) {
            throw new TalaException(
                ErrorCode.INVALID_EVENT_TIME,
                "Event time cannot be in the future"
            );
        }
    }
    
    /**
     * Validate time range
     */
    private void validateTimeRange(Instant startTime, Instant endTime) {
        if (startTime.isAfter(endTime)) {
            throw new TalaException(
                ErrorCode.INVALID_QUERY_PARAMS,
                "Start time must be before end time"
            );
        }
    }
    
    /**
     * Publish event to Kafka
     */
    private void publishEventToKafka(String action, Event event) {
        try {
            String message = String.format(
                "{\"action\":\"%s\",\"eventId\":%d,\"profileId\":%d,\"eventType\":\"%s\",\"eventTime\":\"%s\"}",
                action, event.getId(), event.getProfileId(), 
                event.getEventType(), event.getEventTime()
            );
            
            kafkaTemplate.send(EVENT_TOPIC, event.getProfileId().toString(), message);
            log.debug("Published {} event to Kafka: {}", action, event.getId());
        } catch (Exception e) {
            log.error("Failed to publish event to Kafka", e);
            // Don't throw - Kafka publishing is async and shouldn't fail the request
        }
    }
}
