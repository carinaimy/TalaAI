package com.tala.reminder.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for event-driven reminder creation
 * Only enabled when kafka is explicitly enabled in configuration
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class EventConsumer {
    
    /**
     * Listen to event topic and create reminders
     */
    @KafkaListener(topics = "tala.events", groupId = "reminder-service")
    public void consumeEvent(String message) {
        log.info("Received event: {}", message);
        
        // TODO: Parse event message
        // TODO: Check if reminder should be created based on event type
        // TODO: Extract relevant information
        // TODO: Create reminder via ReminderService
        
        // Example logic:
        // if (eventType == "INCIDENT" || eventType == "TEACHER_NOTE") {
        //     createReminderFromEvent(event);
        // }
    }
}
