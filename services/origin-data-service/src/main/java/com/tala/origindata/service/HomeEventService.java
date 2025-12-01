package com.tala.origindata.service;

import com.tala.origindata.constant.HomeEventType;
import com.tala.origindata.domain.HomeEvent;
import com.tala.origindata.repository.HomeEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing Home Events
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HomeEventService {
    
    private final HomeEventRepository homeEventRepository;
    
    /**
     * Create home event
     */
    @Transactional
    public HomeEvent createHomeEvent(HomeEvent event) {
        HomeEvent saved = homeEventRepository.save(event);
        log.info("Created home event: id={}, profileId={}, type={}", 
                saved.getId(), saved.getProfileId(), saved.getEventType());
        return saved;
    }
    
    /**
     * Get home events by profile
     */
    @Transactional(readOnly = true)
    public List<HomeEvent> getEventsByProfile(Long profileId) {
        return homeEventRepository.findByProfileIdOrderByEventTimeDesc(profileId);
    }
    
    /**
     * Get home events by profile and type
     */
    @Transactional(readOnly = true)
    public List<HomeEvent> getEventsByProfileAndType(Long profileId, HomeEventType eventType) {
        return homeEventRepository.findByProfileIdAndEventTypeOrderByEventTimeDesc(profileId, eventType);
    }
    
    /**
     * Get home events by profile and time range
     */
    @Transactional(readOnly = true)
    public List<HomeEvent> getEventsByProfileAndTimeRange(
            Long profileId, Instant startTime, Instant endTime) {
        return homeEventRepository.findByProfileIdAndEventTimeBetweenOrderByEventTimeDesc(
                profileId, startTime, endTime);
    }
    
    /**
     * Get home event by ID
     */
    @Transactional(readOnly = true)
    public Optional<HomeEvent> getEventById(Long id) {
        return homeEventRepository.findById(id);
    }
    
    /**
     * Get home event by original event ID
     */
    @Transactional(readOnly = true)
    public Optional<HomeEvent> getEventByOriginalEventId(Long originalEventId) {
        return homeEventRepository.findByOriginalEventId(originalEventId);
    }
    
    /**
     * Count home events by profile and type
     */
    @Transactional(readOnly = true)
    public long countByProfileAndType(Long profileId, HomeEventType eventType) {
        return homeEventRepository.countByProfileIdAndEventType(profileId, eventType);
    }
}
