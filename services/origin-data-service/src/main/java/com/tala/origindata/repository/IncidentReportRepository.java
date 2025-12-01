package com.tala.origindata.repository;

import com.tala.origindata.constant.IncidentSeverity;
import com.tala.origindata.domain.IncidentReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Incident Reports
 */
@Repository
public interface IncidentReportRepository extends JpaRepository<IncidentReport, Long> {
    
    List<IncidentReport> findByProfileIdOrderByIncidentTimeDesc(Long profileId);
    
    List<IncidentReport> findByProfileIdAndSeverityOrderByIncidentTimeDesc(Long profileId, IncidentSeverity severity);
    
    List<IncidentReport> findByProfileIdAndIncidentTimeBetweenOrderByIncidentTimeDesc(
        Long profileId, Instant startTime, Instant endTime);
    
    Optional<IncidentReport> findByOriginalEventId(Long originalEventId);
    
    long countByProfileIdAndSeverity(Long profileId, IncidentSeverity severity);
}
