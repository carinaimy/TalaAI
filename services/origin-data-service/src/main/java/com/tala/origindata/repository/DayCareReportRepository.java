package com.tala.origindata.repository;

import com.tala.origindata.domain.DayCareReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Day Care Reports
 */
@Repository
public interface DayCareReportRepository extends JpaRepository<DayCareReport, Long> {
    
    List<DayCareReport> findByProfileIdOrderByReportDateDesc(Long profileId);
    
    List<DayCareReport> findByProfileIdAndReportDateBetweenOrderByReportDateDesc(
        Long profileId, LocalDate startDate, LocalDate endDate);
    
    Optional<DayCareReport> findByOriginalEventId(Long originalEventId);
    
    Optional<DayCareReport> findByProfileIdAndReportDate(Long profileId, LocalDate reportDate);
    
    long countByProfileId(Long profileId);
}
