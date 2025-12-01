package com.tala.origindata.repository;

import com.tala.origindata.constant.TimelineEventType;
import com.tala.origindata.domain.TimelineEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository for Timeline Entries
 */
@Repository
public interface TimelineEntryRepository extends JpaRepository<TimelineEntry, Long> {
    
    Page<TimelineEntry> findByProfileIdOrderByRecordTimeDesc(Long profileId, Pageable pageable);
    
    Page<TimelineEntry> findByProfileIdAndTimelineTypeOrderByRecordTimeDesc(
        Long profileId, TimelineEventType timelineType, Pageable pageable);
    
    List<TimelineEntry> findByProfileIdAndRecordTimeBetweenOrderByRecordTimeDesc(
        Long profileId, Instant startTime, Instant endTime);
    
    List<TimelineEntry> findByOriginalEventId(Long originalEventId);
    
    long countByProfileId(Long profileId);
    
    long countByProfileIdAndTimelineType(Long profileId, TimelineEventType timelineType);
}
