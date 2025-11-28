package com.tala.reminder.mapper;

import com.tala.reminder.domain.Reminder;
import com.tala.reminder.dto.CreateReminderRequest;
import com.tala.reminder.dto.ReminderResponse;
import com.tala.reminder.dto.UpdateReminderRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * MapStruct mapper for Reminder entity
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ReminderMapper {
    
    /**
     * Convert CreateReminderRequest to Reminder entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "snoozeUntil", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    Reminder toEntity(CreateReminderRequest request);
    
    /**
     * Convert Reminder entity to ReminderResponse
     */
    ReminderResponse toResponse(Reminder reminder);
    
    /**
     * Convert list of reminders to responses
     */
    List<ReminderResponse> toResponseList(List<Reminder> reminders);
    
    /**
     * Update existing reminder from request
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "profileId", ignore = true)
    @Mapping(target = "sourceEventId", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "snoozeUntil", ignore = true)
    @Mapping(target = "recurrenceRule", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(UpdateReminderRequest request, @MappingTarget Reminder reminder);
}
