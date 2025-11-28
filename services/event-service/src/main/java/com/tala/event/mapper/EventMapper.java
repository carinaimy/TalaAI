package com.tala.event.mapper;

import com.tala.event.domain.Event;
import com.tala.event.dto.CreateEventRequest;
import com.tala.event.dto.EventResponse;
import com.tala.event.dto.UpdateEventRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

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
}
