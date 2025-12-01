package com.tala.origindata.repository;

import com.tala.origindata.constant.DataSourceType;
import com.tala.origindata.domain.OriginalEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Original Events
 */
@Repository
public interface OriginalEventRepository extends JpaRepository<OriginalEvent, Long> {
    
    List<OriginalEvent> findByProfileIdOrderByEventTimeDesc(Long profileId);
    
    List<OriginalEvent> findByProfileIdAndSourceTypeOrderByEventTimeDesc(Long profileId, DataSourceType sourceType);
    
    List<OriginalEvent> findByProfileIdAndEventTimeBetweenOrderByEventTimeDesc(
        Long profileId, Instant startTime, Instant endTime);
    
    Optional<OriginalEvent> findBySourceTypeAndSourceEventId(DataSourceType sourceType, String sourceEventId);
    
    List<OriginalEvent> findByAiProcessedFalseOrderByEventTimeAsc();
    
    long countByProfileIdAndSourceType(Long profileId, DataSourceType sourceType);
}
