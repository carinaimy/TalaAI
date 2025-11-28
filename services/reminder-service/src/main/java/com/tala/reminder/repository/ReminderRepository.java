package com.tala.reminder.repository;

import com.tala.reminder.domain.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Reminder Repository
 */
@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    
    /**
     * Find by ID excluding soft-deleted
     */
    @Query("SELECT r FROM Reminder r WHERE r.id = :id AND r.deletedAt IS NULL")
    Optional<Reminder> findByIdAndNotDeleted(@Param("id") Long id);
    
    /**
     * Find active reminders by user and profile
     */
    @Query("""
        SELECT r FROM Reminder r 
        WHERE r.userId = :userId 
          AND (:profileId IS NULL OR r.profileId = :profileId)
          AND r.status IN ('ACTIVE', 'SNOOZED')
          AND r.deletedAt IS NULL
        ORDER BY r.dueAt ASC
        """)
    List<Reminder> findActiveByUserAndProfile(
        @Param("userId") Long userId,
        @Param("profileId") Long profileId
    );
    
    /**
     * Find reminders due within time range
     */
    @Query("""
        SELECT r FROM Reminder r 
        WHERE r.userId = :userId 
          AND r.status = 'ACTIVE'
          AND r.dueAt BETWEEN :startTime AND :endTime
          AND (r.snoozeUntil IS NULL OR r.snoozeUntil < :now)
          AND r.deletedAt IS NULL
        ORDER BY r.dueAt ASC
        """)
    List<Reminder> findDueReminders(
        @Param("userId") Long userId,
        @Param("startTime") Instant startTime,
        @Param("endTime") Instant endTime,
        @Param("now") Instant now
    );
    
    /**
     * Find reminders by source event
     */
    @Query("""
        SELECT r FROM Reminder r 
        WHERE r.sourceEventId = :eventId 
          AND r.deletedAt IS NULL
        """)
    List<Reminder> findBySourceEvent(@Param("eventId") Long eventId);
    
    /**
     * Find reminders by category
     */
    @Query("""
        SELECT r FROM Reminder r 
        WHERE r.userId = :userId 
          AND r.category = :category
          AND r.status IN ('ACTIVE', 'SNOOZED')
          AND r.deletedAt IS NULL
        ORDER BY r.dueAt ASC
        """)
    List<Reminder> findByCategory(
        @Param("userId") Long userId,
        @Param("category") String category
    );
    
    /**
     * Count active reminders
     */
    @Query("""
        SELECT COUNT(r) FROM Reminder r 
        WHERE r.userId = :userId 
          AND r.status = 'ACTIVE'
          AND r.deletedAt IS NULL
        """)
    long countActiveByUser(@Param("userId") Long userId);
}
