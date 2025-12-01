package com.tala.origindata.repository;

import com.tala.origindata.constant.HealthReportType;
import com.tala.origindata.domain.HealthReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Health Reports
 */
@Repository
public interface HealthReportRepository extends JpaRepository<HealthReport, Long> {
    
    List<HealthReport> findByProfileIdOrderByVisitTimeDesc(Long profileId);
    
    List<HealthReport> findByProfileIdAndReportTypeOrderByVisitTimeDesc(Long profileId, HealthReportType reportType);
    
    List<HealthReport> findByProfileIdAndVisitTimeBetweenOrderByVisitTimeDesc(
        Long profileId, Instant startTime, Instant endTime);
    
    Optional<HealthReport> findByOriginalEventId(Long originalEventId);
    
    long countByProfileIdAndReportType(Long profileId, HealthReportType reportType);
}
