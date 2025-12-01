package com.tala.origindata.repository;

import com.tala.origindata.constant.HomeEventType;
import com.tala.origindata.domain.HomeEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Home Events
 */
@Repository
public interface HomeEventRepository extends JpaRepository<HomeEvent, Long> {
    
    List<HomeEvent> findByProfileIdOrderByEventTimeDesc(Long profileId);
    
    List<HomeEvent> findByProfileIdAndEventTypeOrderByEventTimeDesc(Long profileId, HomeEventType eventType);
    
    List<HomeEvent> findByProfileIdAndEventTimeBetweenOrderByEventTimeDesc(
        Long profileId, Instant startTime, Instant endTime);
    
    Optional<HomeEvent> findByOriginalEventId(Long originalEventId);
    
    long countByProfileIdAndEventType(Long profileId, HomeEventType eventType);
}
