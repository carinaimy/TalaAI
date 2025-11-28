package com.tala.query.repository;

import com.tala.query.domain.DailyChildSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Daily Child Summary Repository
 */
@Repository
public interface DailyChildSummaryRepository extends JpaRepository<DailyChildSummary, Long> {
    
    /**
     * Find by profile and date
     */
    @Query("SELECT d FROM DailyChildSummary d WHERE d.profileId = :profileId AND d.date = :date AND d.deletedAt IS NULL")
    Optional<DailyChildSummary> findByProfileIdAndDate(
        @Param("profileId") Long profileId,
        @Param("date") LocalDate date
    );
    
    /**
     * Find by profile and date range
     */
    @Query("""
        SELECT d FROM DailyChildSummary d 
        WHERE d.profileId = :profileId 
          AND d.date BETWEEN :startDate AND :endDate 
          AND d.deletedAt IS NULL
        ORDER BY d.date DESC
        """)
    List<DailyChildSummary> findByProfileIdAndDateRange(
        @Param("profileId") Long profileId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    /**
     * Find recent summaries
     */
    @Query("""
        SELECT d FROM DailyChildSummary d 
        WHERE d.profileId = :profileId 
          AND d.deletedAt IS NULL
        ORDER BY d.date DESC
        LIMIT :limit
        """)
    List<DailyChildSummary> findRecentByProfileId(
        @Param("profileId") Long profileId,
        @Param("limit") int limit
    );
    
    /**
     * Find days with incidents
     */
    @Query("""
        SELECT d FROM DailyChildSummary d 
        WHERE d.profileId = :profileId 
          AND d.hasIncident = true
          AND d.deletedAt IS NULL
        ORDER BY d.date DESC
        """)
    List<DailyChildSummary> findIncidentDays(@Param("profileId") Long profileId);
}
