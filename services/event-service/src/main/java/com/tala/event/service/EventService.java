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
     * Get timeline events grouped by date
     */
    @Transactional(readOnly = true)
    public List<com.tala.event.dto.TimelineEventResponse> getTimelineEvents(
        Long profileId,
        Instant startTime,
        Instant endTime,
        List<String> eventTypes
    ) {
        validateTimeRange(startTime, endTime);
        
        List<Event> events;
        if (eventTypes != null && !eventTypes.isEmpty()) {
            events = new java.util.ArrayList<>();
            for (String type : eventTypes) {
                events.addAll(repository.findByProfileIdAndEventTypeAndTimeRange(
                    profileId, type, startTime, endTime
                ));
            }
        } else {
            events = repository.findByProfileIdAndTimeRange(
                profileId, startTime, endTime
            );
        }
        
        return events.stream()
            .map(this::toTimelineResponse)
            .sorted((a, b) -> b.getEventTime().compareTo(a.getEventTime()))
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Get calendar month summary
     */
    @Transactional(readOnly = true)
    public com.tala.event.dto.CalendarMonthResponse getCalendarMonth(
        Long profileId,
        java.time.YearMonth yearMonth
    ) {
        java.time.LocalDate startDate = yearMonth.atDay(1);
        java.time.LocalDate endDate = yearMonth.atEndOfMonth();
        
        Instant startTime = startDate.atStartOfDay(java.time.ZoneId.of("UTC")).toInstant();
        Instant endTime = endDate.plusDays(1).atStartOfDay(java.time.ZoneId.of("UTC")).toInstant();
        
        List<Event> events = repository.findByProfileIdAndTimeRange(
            profileId, startTime, endTime
        );
        
        // Group by date
        java.util.Map<java.time.LocalDate, List<Event>> eventsByDate = events.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                e -> e.getEventTime().atZone(java.time.ZoneId.of("UTC")).toLocalDate()
            ));
        
        List<com.tala.event.dto.CalendarDaySummary> daySummaries = new java.util.ArrayList<>();
        
        for (java.time.LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            List<Event> dayEvents = eventsByDate.getOrDefault(date, java.util.Collections.emptyList());
            daySummaries.add(buildDaySummary(date, dayEvents));
        }
        
        return com.tala.event.dto.CalendarMonthResponse.builder()
            .yearMonth(yearMonth)
            .profileId(profileId)
            .days(daySummaries)
            .totalEvents((long) events.size())
            .build();
    }
    
    private com.tala.event.dto.TimelineEventResponse toTimelineResponse(Event event) {
        return com.tala.event.dto.TimelineEventResponse.builder()
            .id(event.getId())
            .profileId(event.getProfileId())
            .eventType(event.getEventType())
            .eventTime(event.getEventTime())
            .eventDate(event.getEventTime().atZone(java.time.ZoneId.of("UTC")).toLocalDate())
            .aiSummary(event.getAiSummary())
            .aiTags(event.getAiTags())
            .priority(event.getPriority())
            .urgencyHours(event.getUrgencyHours())
            .riskLevel(event.getRiskLevel())
            .eventData(event.getEventData())
            .build();
    }
    
    private com.tala.event.dto.CalendarDaySummary buildDaySummary(
        java.time.LocalDate date,
        List<Event> events
    ) {
        java.util.Map<String, Long> typeCount = events.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                Event::getEventType,
                java.util.stream.Collectors.counting()
            ));
        
        boolean hasIncident = events.stream().anyMatch(e -> "INCIDENT".equals(e.getEventType()));
        boolean hasSickness = events.stream().anyMatch(e -> "SICKNESS".equals(e.getEventType()));
        boolean hasReminder = events.stream().anyMatch(e -> "REMINDER_CREATED".equals(e.getEventType()));
        boolean hasMedicalVisit = events.stream().anyMatch(e -> "MEDICAL_VISIT".equals(e.getEventType()));
        
        boolean hasImportant = hasIncident || hasSickness || hasMedicalVisit ||
            events.stream().anyMatch(e -> "high".equals(e.getPriority()) || "critical".equals(e.getPriority()));
        
        java.util.List<String> highlightTags = events.stream()
            .filter(e -> e.getAiTags() != null)
            .flatMap(e -> e.getAiTags().stream())
            .distinct()
            .limit(5)
            .collect(java.util.stream.Collectors.toList());
        
        return com.tala.event.dto.CalendarDaySummary.builder()
            .date(date)
            .totalEvents((long) events.size())
            .hasImportantEvent(hasImportant)
            .hasIncident(hasIncident)
            .hasSickness(hasSickness)
            .hasReminder(hasReminder)
            .hasMedicalVisit(hasMedicalVisit)
            .eventTypeCount(typeCount)
            .highlightTags(highlightTags)
            .build();
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
