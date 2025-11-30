package com.tala.event.mapper;

import com.tala.event.domain.Event;
import com.tala.event.dto.CreateEventRequest;
import com.tala.event.dto.EventResponse;
import com.tala.event.dto.UpdateEventRequest;
import org.mapstruct.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MapStruct mapper for Event entity
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface EventMapper {
    
    /**
     * Convert CreateEventRequest to Event entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "aiTags", ignore = true)
    @Mapping(target = "priority", source = "priority")
    @Mapping(target = "urgencyHours", source = "urgencyHours")
    @Mapping(target = "riskLevel", source = "riskLevel")
    Event toEntity(CreateEventRequest request);
    
    /**
     * Convert Event entity to EventResponse
     */
    @Mapping(target = "priority", source = "priority")
    @Mapping(target = "urgencyHours", source = "urgencyHours")
    @Mapping(target = "riskLevel", source = "riskLevel")
    EventResponse toResponse(Event event);
    
    /**
     * Convert list of events to responses
     */
    List<EventResponse> toResponseList(List<Event> events);
    
    /**
     * Update existing event from request
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profileId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "eventType", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "aiTags", ignore = true)
    @Mapping(target = "source", ignore = true)
    @Mapping(target = "priority", source = "priority")
    @Mapping(target = "urgencyHours", source = "urgencyHours")
    @Mapping(target = "riskLevel", source = "riskLevel")
    void updateEntity(UpdateEventRequest request, @MappingTarget Event event);
    
    /**
     * After mapping: Convert notes/metadata to eventData for storage
     */
    @AfterMapping
    default void mergeNotesAndMetadata(CreateEventRequest request, @MappingTarget Event event) {
        Map<String, Object> eventData = new HashMap<>();
        
        // If eventData is provided, use it as base
        if (request.getEventData() != null) {
            eventData.putAll(request.getEventData());
        }
        
        // Add notes if provided
        if (request.getNotes() != null && !request.getNotes().isEmpty()) {
            eventData.put("notes", request.getNotes());
        }
        
        // Add metadata if provided
        if (request.getMetadata() != null && !request.getMetadata().isEmpty()) {
            eventData.putAll(request.getMetadata());
        }
        
        event.setEventData(eventData);
    }
    
    /**
     * After mapping: Extract notes/metadata from eventData for response
     */
    @AfterMapping
    default void extractNotesAndMetadata(Event event, @MappingTarget EventResponse response) {
        Map<String, Object> eventData = event.getEventData();
        
        if (eventData != null) {
            // Extract notes
            Object notes = eventData.get("notes");
            if (notes != null) {
                response.setNotes(notes.toString());
            }
            
            // Create metadata map (all fields except notes)
            Map<String, Object> metadata = new HashMap<>(eventData);
            metadata.remove("notes");
            response.setMetadata(metadata);
            
            // Keep eventData for backward compatibility
            response.setEventData(eventData);
        }
    }
}
