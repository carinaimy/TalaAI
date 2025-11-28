package com.tala.reminder.controller;

import com.tala.reminder.dto.CreateReminderRequest;
import com.tala.reminder.dto.ReminderResponse;
import com.tala.reminder.dto.UpdateReminderRequest;
import com.tala.reminder.service.ReminderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 * REST API for reminders
 */
@RestController
@RequestMapping("/api/v1/reminders")
@RequiredArgsConstructor
@Slf4j
public class ReminderController {
    
    private final ReminderService service;
    
    /**
     * Create new reminder
     */
    @PostMapping
    public ResponseEntity<ReminderResponse> create(
        @Valid @RequestBody CreateReminderRequest request
    ) {
        log.info("POST /api/v1/reminders - Creating reminder");
        ReminderResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get reminder by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReminderResponse> getById(@PathVariable Long id) {
        log.debug("GET /api/v1/reminders/{}", id);
        ReminderResponse response = service.getById(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update reminder
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReminderResponse> update(
        @PathVariable Long id,
        @Valid @RequestBody UpdateReminderRequest request
    ) {
        log.info("PUT /api/v1/reminders/{}", id);
        ReminderResponse response = service.update(id, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete reminder
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/v1/reminders/{}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Complete reminder
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<ReminderResponse> complete(@PathVariable Long id) {
        log.info("POST /api/v1/reminders/{}/complete", id);
        ReminderResponse response = service.complete(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Snooze reminder
     */
    @PostMapping("/{id}/snooze")
    public ResponseEntity<ReminderResponse> snooze(
        @PathVariable Long id,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant until
    ) {
        log.info("POST /api/v1/reminders/{}/snooze until {}", id, until);
        ReminderResponse response = service.snooze(id, until);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Cancel reminder
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ReminderResponse> cancel(@PathVariable Long id) {
        log.info("POST /api/v1/reminders/{}/cancel", id);
        ReminderResponse response = service.cancel(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get active reminders
     */
    @GetMapping
    public ResponseEntity<List<ReminderResponse>> getActiveReminders(
        @RequestParam Long userId,
        @RequestParam(required = false) Long profileId
    ) {
        log.debug("GET /api/v1/reminders - userId={}, profileId={}", userId, profileId);
        List<ReminderResponse> reminders = service.getActiveReminders(userId, profileId);
        return ResponseEntity.ok(reminders);
    }
    
    /**
     * Get due reminders
     */
    @GetMapping("/due")
    public ResponseEntity<List<ReminderResponse>> getDueReminders(
        @RequestParam Long userId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime
    ) {
        log.debug("GET /api/v1/reminders/due - userId={}", userId);
        List<ReminderResponse> reminders = service.getDueReminders(userId, startTime, endTime);
        return ResponseEntity.ok(reminders);
    }
    
    /**
     * Get reminders by category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ReminderResponse>> getByCategory(
        @PathVariable String category,
        @RequestParam Long userId
    ) {
        log.debug("GET /api/v1/reminders/category/{} - userId={}", category, userId);
        List<ReminderResponse> reminders = service.getRemindersByCategory(userId, category);
        return ResponseEntity.ok(reminders);
    }
    
    /**
     * Count active reminders
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countActive(@RequestParam Long userId) {
        log.debug("GET /api/v1/reminders/count - userId={}", userId);
        long count = service.countActiveReminders(userId);
        return ResponseEntity.ok(count);
    }
}
