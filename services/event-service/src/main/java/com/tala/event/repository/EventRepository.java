package com.tala.event.repository;

import com.tala.event.domain.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Event Repository
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    /**
     * Find by ID excluding soft-deleted
     */
    @Query("SELECT e FROM Event e WHERE e.id = :id AND e.deletedAt IS NULL")
    Optional<Event> findByIdAndNotDeleted(@Param("id") Long id);
    
    /**
     * Find events by profile and date range
     */
    @Query("""
        SELECT e FROM Event e 
        WHERE e.profileId = :profileId 
          AND e.eventTime BETWEEN :startTime AND :endTime 
          AND e.deletedAt IS NULL
        ORDER BY e.eventTime DESC
        """)
    List<Event> findByProfileIdAndTimeRange(
        @Param("profileId") Long profileId,
        @Param("startTime") Instant startTime,
        @Param("endTime") Instant endTime
    );
    
    /**
     * Find events by profile, type, and date range
     */
    @Query("""
        SELECT e FROM Event e 
        WHERE e.profileId = :profileId 
          AND e.eventType = :eventType
          AND e.eventTime BETWEEN :startTime AND :endTime 
          AND e.deletedAt IS NULL
        ORDER BY e.eventTime DESC
        """)
    List<Event> findByProfileIdAndEventTypeAndTimeRange(
        @Param("profileId") Long profileId,
        @Param("eventType") String eventType,
        @Param("startTime") Instant startTime,
        @Param("endTime") Instant endTime
    );
    
    /**
     * Count events by profile and date range
     */
    @Query("""
        SELECT COUNT(e) FROM Event e 
        WHERE e.profileId = :profileId 
          AND e.eventTime BETWEEN :startTime AND :endTime 
          AND e.deletedAt IS NULL
        """)
    long countByProfileIdAndTimeRange(
        @Param("profileId") Long profileId,
        @Param("startTime") Instant startTime,
        @Param("endTime") Instant endTime
    );
    
    /**
     * Find recent events with pagination
     */
    Page<Event> findByProfileIdAndDeletedAtIsNullOrderByEventTimeDesc(
        Long profileId, 
        Pageable pageable
    );
    
    /**
     * Find latest event by type
     */
    @Query("""
        SELECT e FROM Event e 
        WHERE e.profileId = :profileId 
          AND e.eventType = :eventType 
          AND e.deletedAt IS NULL
        ORDER BY e.eventTime DESC
        LIMIT 1
        """)
    Optional<Event> findLatestByProfileIdAndEventType(
        @Param("profileId") Long profileId,
        @Param("eventType") String eventType
    );
}
